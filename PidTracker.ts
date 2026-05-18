import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import {type Plugin} from 'vite';
import dedent from "dedent";

interface IncompleteTapeGalleryPidManagerPluginConfig {
    name: string;
}

interface TapeGalleryPidManagerPluginConfig extends IncompleteTapeGalleryPidManagerPluginConfig{
    name: string;
    pid: number;
    startedAt: Date;
    pidFilePath: string;
}

interface PidManagerInitializedKeyInfo {
    initResultedInError: boolean;
    pluginConfig: IncompleteTapeGalleryPidManagerPluginConfig | TapeGalleryPidManagerPluginConfig;
}

interface PidFileContent {
    pid: number | null;
    startedAt: Date;
    command: string;
}

const PIDS_DIR: string = path.resolve(__dirname, '.pids');

export function pidManagerPlugin(): Plugin {
    const { initResultedInError, pluginConfig }: PidManagerInitializedKeyInfo = initPidManagerKeyInfo();
    if (initResultedInError) {
        return pluginConfig;
    }

    const { pid, startedAt, pidFilePath } = pluginConfig as TapeGalleryPidManagerPluginConfig;

    /**
     * Creates the pid file as JSON containing following information:
     * <ul>
     *     <li>pid</li>
     *     <li>startedAt</li>
     *     <li>command</li>
     * </ul>
     */
    function writePidFile(): void {
        fs.mkdirSync(PIDS_DIR, { recursive: true });
        const pidFileContent: PidFileContent = {
            pid,
            startedAt,
            command: process.argv.join(' '),
        }
        const content = JSON.stringify(pidFileContent, null, 2);
        fs.writeFileSync(pidFilePath, content, 'utf-8');
    }

    function removePidFileOnProcessExit(code: number): void {
        try {
            console.warn(`Received following code: ${code}`);
            console.warn(dedent `
                Removing ${pid}.
                Exit code: ${code}`);
            const pidFiles: string[] = fs.readdirSync(PIDS_DIR)
                                         .filter(f => f.endsWith('.pid')
                                                             && f.includes(pid.toString()));
            pidFiles.forEach(fileWithCurrentPid => {
                const fullPath: string = path.resolve(PIDS_DIR, fileWithCurrentPid);
                fs.unlinkSync(fullPath);
            });
        } catch (e) {
            const enoentIncluded: boolean = (e as Error).message.includes("ENOENT");
            !enoentIncluded && console.error(`Unable to remove pid file (${pid}): `, e);
        }
    }

    // Does not catch OS signals
    process.on('exit', (code: number) => removePidFileOnProcessExit(code));
    process.on('uncaughtException', (error) => {
        console.error("Process hook 'uncaughtException' caught: ", error)
    });

    return {
        ...pluginConfig,
        configureServer() {
            writePidFile();
            trackActiveInstances(pid, pidFilePath, startedAt);
        }
    }
}

const initPidManagerKeyInfo:() => PidManagerInitializedKeyInfo = (): PidManagerInitializedKeyInfo => {
    const initResult: PidManagerInitializedKeyInfo = {
        initResultedInError: true,
        pluginConfig: {name: 'tapegallery-pid-tracker'}
    }

    const pid: number = process.pid;
    const processStartDate: Date = new Date();
    let pidFileName: string = "";

    try {
        pidFileName = createPidFileName(processStartDate, pid);
    } catch (err) {
        console.error(err);
        return initResult;
    }

    let pidFilePath: string;
    try {
        pidFilePath = path.resolve(PIDS_DIR, pidFileName)
    } catch (err) {
        console.error(`Error when resolving path to pid file ${pidFileName}`, err);
        return initResult;
    }

    return {
        initResultedInError: false,
        pluginConfig: {
            name: initResult.pluginConfig.name,
            pid,
            startedAt: processStartDate,
            pidFilePath
        }
    };
}

/**
 * Tracks active instances of this application by searching for stale pid files inside
 * the <code>.pids</code> folder that is created whenever this app starts.
 */
function trackActiveInstances(currentPidNumber: PidFileContent['pid'], currentPidFilePath: string, processStartDate: Date) {
    fs.mkdirSync(PIDS_DIR, { recursive: true });
    console.log(`Start date: ${processStartDate}`)
    console.log(`Current pid: ${currentPidNumber}`)
    const currentPidFilename: string = path.basename(currentPidFilePath);
    // simply remove the new pid file from all read pid files
    const stalePidFiles: string[] = fs.readdirSync(PIDS_DIR)
                                 .filter(f => !f.includes(currentPidFilename));
    stalePidFiles.forEach(stalePidFile => {
        let stalePidNumber!: PidFileContent['pid'] ;
        try {
            const pathToStalePidFile: string = path.resolve(PIDS_DIR, stalePidFile);
            const pidFileContent: string = fs.readFileSync(pathToStalePidFile).toString();
            const parsedPidFileContent: PidFileContent = JSON.parse(pidFileContent);
            stalePidNumber = parsedPidFileContent.pid;

            if (typeof stalePidNumber === "number") {
                // signal == 0 to check process still exists
                try {
                    process.kill(stalePidNumber, 0);
                    // if "process.kill(..., 0)" doesn't throw an error, then it's killable
                    process.kill(stalePidNumber, 9);
                } catch (e) {
                    // Do nothing when 'Error: kill ESRCH;' is returned.
                    // This indicates that no such process is running.
                    // Otherwise, surrounding catch will print this error every time.
                }
                console.log(`Checked and/or killed process for pid: ${stalePidNumber}`);
            }
        } catch (err) {
            console.error("Caught error when checking pid files:", err);
        }
        unlinkStalePidFile(stalePidNumber, stalePidFile);
    });

    console.info(`[PID Tracker] Process ${currentPidNumber} started at '${processStartDate}'`)
}

function unlinkStalePidFile(pidNumber: PidFileContent['pid'], pidFilename: string): void {
    if (!(typeof pidNumber === "number")) {
        console.error(dedent `PID number for file 
            '${pidFilename}'
        not of type 'number'`);
        return;
    }
    // stale pid file — clean it up
    console.info(`START: Clean up stale pid file: ${pidNumber}`);
    fs.unlinkSync(path.resolve(PIDS_DIR, pidFilename));
    console.info(`END: Clean up stale pid file: ${pidNumber}`);
}

/**
 * Creates a filename for the pid-file.
 * @param {Date} inputDate a date to add as a timestamp to the filename
 * @param {PidFileContent['pid']} pidNumber the PID number
 * @returns {string} a filename in the following format:
 *                   ${year}-${month}-${day}--${hour}-${minute}-${second}_${pidNumber}.pid
 */
function createPidFileName(inputDate: Date, pidNumber: PidFileContent['pid']): string {
    const formattedDateParts: Intl.DateTimeFormatPart[] = new Intl.DateTimeFormat(undefined, {
        hour: '2-digit',
        hour12: false,
        hourCycle: 'h24',
        minute: '2-digit',
        second: '2-digit',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    }).formatToParts(inputDate);

    const { year, month, day, hour, minute, second } = formattedDateParts
        .reduce((containerRecord, currentValue) => {
                // 'literal' means "/" aka. the dividing char of that date object
                const type = currentValue.type;
                if (type !== 'literal') {
                    // e.g containerRecord[type] = current.value is equals to { ... "day": "18" ... }
                    containerRecord[type] = currentValue.value;
                }
                // is returned to keep populating the same object
                return containerRecord;
            },
            // Typescript requires explicit typing here
            {} as Record<string, string>);

    const pidFileTimestamp: string = `${year}-${month}-${day}--${hour}-${minute}-${second}`;
    if (pidNumber !== null) {
        return `${pidFileTimestamp}_${pidNumber}.pid`;
    }

    throw new Error(`Invalid pid number: ${pidNumber}`);
}
