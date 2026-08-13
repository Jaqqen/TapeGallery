# TapeGallery — Operations Guide

How to develop, test, deploy and use TapeGallery.

> Status: 🛠️ In development. Everything below reflects the repository as it actually is today —
> where something is not yet set up (notably automated tests and CI/CD), that is stated explicitly
> rather than described as if it existed.

---

## 1. What this project is

TapeGallery is a single-page React application that renders a shelf of retro VHS tapes. Tapes can be
selected, collected in a tray, and opened in a 3D-flip detail view.

| Aspect | Value |
| --- | --- |
| Framework | React 19 + TypeScript |
| Build tool | Vite 8 (`@vitejs/plugin-react-swc`) |
| Animation | Framer Motion 12 |
| Linting | ESLint 9 (flat config, `typescript-eslint`) |
| Backend | None — fully static, no API, no database |
| Data source | Hard-coded array in `src/data/tapes.ts` (12 tapes) |

### Repository layout

```
.
├── index.html                  # Vite HTML entry point
├── vite.config.ts              # React SWC plugin + custom PID tracker plugin
├── PidTracker.ts               # Custom Vite plugin: kills stale dev-server processes
├── eslint.config.js            # ESLint flat config
├── tsconfig.json               # Solution file → app + node configs
├── tsconfig.app.json           # Config for src/ (browser)
├── tsconfig.node.json          # Config for vite.config.ts / tooling (node)
├── src/
│   ├── main.tsx                # React root
│   ├── App.tsx                 # State: selection Set + detail tape
│   ├── App.css, index.css      # All styling (plain CSS, CSS custom properties)
│   ├── components/
│   │   ├── ShelfVhsTape.tsx    # One tape on the shelf + SVG pattern renderer
│   │   ├── VhsTapeDetail.tsx   # Full-screen detail overlay
│   │   └── VhsTapeTray.tsx     # Bottom tray of selected tapes
│   └── data/tapes.ts           # Tape interface + tape catalogue
├── external/screenshots/       # Screenshots used by README.md
├── public/                     # Static assets copied verbatim into dist/
├── dist/                       # Build output (generated, do not edit)
└── .pids/                      # Runtime PID files (generated, git-ignored)
```

---

## 2. Development

### 2.1 Prerequisites

- **Node.js ≥ 20.19** (Vite 8 requirement). Verified working on **v24.13.0**.
- **npm** (verified on 11.14.1). `web-portal/package-lock.json` is intentionally git-ignored, so lockfile-based
  installs (`npm ci`) are not available — use `npm install`.

### 2.2 First-time setup

```bash
git clone <repository-url>
cd TapeGallery
npm install
```

### 2.3 Run the dev server

```bash
npm run dev
```

Serves on **http://localhost:555** with hot module replacement. The port is pinned in the `dev`
script (`vite --port 555`); override per-run with `npm run dev -- --port 5173`, or expose it on your
network with `npm run dev -- --host`.

### 2.4 The PID tracker (`PidTracker.ts`)

This project ships a custom Vite plugin that prevents stale dev servers from piling up. It is
registered in `vite.config.ts` and behaves as follows:

1. On dev-server start it writes `.pids/<YYYY-MM-DD--HH-mm-ss>_<pid>.pid`, a JSON file containing
   `pid`, `startedAt` and the full `command`.
2. It then scans `web-portal/.pids` for every *other* PID file, checks whether that process is still alive
   (`process.kill(pid, 0)`) and, if so, **kills it with SIGKILL**, then deletes the file.
3. On a clean `process.exit` it removes its own PID file.

Practical consequences:

- **Only one dev server can run at a time.** Starting a second one terminates the first. This is
  intended behaviour, not a bug.
- The `exit` hook does not catch OS signals, so a `kill -9` on the dev server leaves an orphaned
  `.pid` file behind. The next start cleans it up — harmless, but that is why you may see stale files
  in `web-portal/.pids`.
- If PID files ever get out of sync, `rm -rf .pids` is a safe reset. The directory is git-ignored and
  recreated on the next start.
- The plugin also runs during `npm run build`, which is why the build log ends with
  `Removing <pid>. Exit code: 0`.

### 2.5 Common development tasks

**Add a tape.** Append an object to the `tapes` array in `src/data/tapes.ts`. The `Tape` interface
requires `id`, `title`, `year`, `genre`, `duration`, `rating`, `description`, a four-colour `colors`
object (`primary`, `secondary`, `accent`, `label`) and a `pattern`. `subtitle` is optional.
`id` must be unique — it is the React key and the Framer Motion `layoutId` prefix, so duplicates
break the shelf→detail transition.

**Add a tape pattern.** Patterns are a closed union in `src/data/tapes.ts`
(`"stripes" | "gradient" | "geometric" | "retro-blocks" | "waves" | "diamonds"`). To add one, extend
the union *and* add a matching `case` to the `TapePattern` switch in `src/components/ShelfVhsTape.tsx`.
TypeScript will not force you to handle the new case, so do both in one change.

**Change styling.** All CSS lives in `src/index.css` (global/reset) and `src/App.css` (everything
else). Per-tape colours are passed down as CSS custom properties (e.g. `--tape-primary`) via inline
`style` props, so components stay colour-agnostic.

### 2.6 Git workflow

- Main branch: `master`. Feature branches follow `<issue-number>-<snake_case_description>`, e.g.
  `7-feature_manager_for_stale_background_processes`.
- Issue templates for bugs and features live in `.github/ISSUE_TEMPLATE`.
- Do **not** commit `web-portal/dist` or `web-portal/.pids`. `web-portal/dist` is currently untracked but *not* git-ignored — add it
  to `.gitignore` if it keeps showing up in `git status`.

---

## 3. Test

### 3.1 Current state — read this first

**There is no automated test suite in this repository.** No test runner is installed, no test files
exist, and there is no `npm test` script. Quality gates today are the type checker, the linter and
manual verification.

### 3.2 What you can run today

| Command | What it does | Current result |
| --- | --- | --- |
| `npm run lint` | ESLint across the whole repo | **Fails — 3 errors** (see below) |
| `npm run build` | `tsc -b` (type-check) then production bundle | **Passes** |
| `npm run preview` | Serves `web-portal/dist` as it would be served in production | Passes |

Type-checking without emitting a bundle: `npx tsc -b`.

**Known lint failures** (present on `master` as of this writing — not introduced by you):

```
PidTracker.ts  73:13  @typescript-eslint/no-unused-expressions
PidTracker.ts 154:26  @typescript-eslint/no-unused-vars        ('e' in the empty catch)
src/App.tsx    17:13  @typescript-eslint/no-unused-expressions  (ternary used as statement)
```

All three are the short-circuit/ternary-as-statement idiom. Either rewrite them as `if` statements
or relax the rules in `eslint.config.js` — but do it deliberately, because until then the lint gate
cannot be used as a pass/fail signal in CI.

### 3.3 Manual test checklist

Run `npm run dev` and verify:

- [ ] All 12 tapes render on the shelf with their distinct colours and patterns.
- [ ] Hovering a tape lifts and slightly scales it.
- [ ] Single-click toggles selection; the tape shows its selected state.
- [ ] The bottom tray appears on first selection and shows the correct count and pluralisation
      ("1 tape selected" / "2 tapes selected").
- [ ] Removing a chip via `×` deselects that tape; "Clear All" empties the tray and it slides away.
- [ ] Double-click opens the detail overlay with the tape flipping in.
- [ ] Clicking the overlay backdrop or the close button dismisses it; clicking inside the tape does not.
- [ ] A tape already open in detail view ignores hover/click selection.
- [ ] Resize the window — the grid reflows without overflow.
- [ ] Browser console is free of React key warnings and errors.

Then run `npm run build && npm run preview` and repeat the checklist against the production bundle —
Framer Motion layout animations behave differently under React StrictMode in dev than in production.

### 3.4 Recommended setup when you add tests

Vitest is the natural fit (shares Vite's config and transform pipeline):

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom
```

Add to `package.json`:

```json
"scripts": {
  "test": "vitest run",
  "test:watch": "vitest"
}
```

Highest-value first tests, given the current code:

- `App.tsx` selection logic — toggle adds/removes, `clearAll` empties, `removeSelected` removes one.
- `VhsTapeTray` — count pluralisation and the `onRemove` / `onClear` callbacks.
- `data/tapes.ts` — every `id` is unique and every `pattern` is handled by `TapePattern`.

---

## 4. Deployment

TapeGallery compiles to a fully static site — HTML, CSS, JS and images, no server runtime.

### 4.1 Build

```bash
npm install
npm run build
```

`tsc -b` type-checks first and aborts the build on type errors; `vite build` then emits to `web-portal/dist`:

```
dist/index.html                 ~0.45 kB
dist/assets/index-<hash>.css    ~9.7 kB   (2.6 kB gzip)
dist/assets/index-<hash>.js     ~328 kB   (104 kB gzip)
dist/vite.svg
```

Asset filenames are content-hashed, so `dist/assets/*` can be cached aggressively while
`web-portal/dist/index.html` must be served with a short or no-cache policy.

### 4.2 Verify the build locally

```bash
npm run preview
```

This serves the real `web-portal/dist` output. Always do this before shipping — it is the only way to catch
production-only issues (asset paths, minification, non-StrictMode animation behaviour).

### 4.3 Publish

Deploy the **contents of `web-portal/dist`** to any static host. Since the app is a single route with no
client-side router, no SPA rewrite rules are required.

- **Netlify / Vercel / Cloudflare Pages** — build command `npm run build`, publish directory `dist`.
- **GitHub Pages** — push `web-portal/dist` to `gh-pages`. If the site is served from a subpath
  (`https://<user>.github.io/TapeGallery/`), you must set `base: '/TapeGallery/'` in `vite.config.ts`,
  otherwise every asset 404s.
- **Any web server (nginx, Apache, S3+CloudFront)** — copy `web-portal/dist` to the document root.

### 4.4 Deployment checklist

- [ ] Working tree clean and on the intended commit.
- [ ] `npm run build` succeeds with no type errors.
- [ ] `npm run preview` verified against the manual checklist in §3.3.
- [ ] `base` in `vite.config.ts` matches the deployment path (only needed for subpath hosting).
- [ ] `web-portal/dist` contents uploaded — not the `web-portal/dist` folder itself, and not the repository root.
- [ ] `index.html` served with no long-lived cache; `assets/` served with a long cache.

### 4.5 Not yet in place

There is **no CI/CD pipeline** — `.github` contains only issue templates, no workflows. Builds and
deploys are manual today. When you add a workflow, note that `web-portal/package-lock.json` is git-ignored, so
the job must use `npm install`, not `npm ci`, until that changes.

---

## 5. User guide

TapeGallery is a browsing interface for a shelf of retro VHS tapes. It works in any modern browser
and needs no account, installation or network connection after load.

### 5.1 The screen

- **Header** — the TAPE GALLERY title and a reminder of the two main gestures.
- **Shelf** — the grid of tapes. Each tape shows its title, subtitle, year, genre and rating on a
  colour-coded label.
- **Tray** — appears along the bottom once you select at least one tape.

### 5.2 Actions

| Action | Gesture | Result |
| --- | --- | --- |
| Preview a tape | **Hover** | The tape lifts slightly off the shelf |
| Select / deselect | **Single click** | The tape is added to or removed from the tray |
| Open details | **Double click** | The tape flips open into a full-screen detail view |
| Close details | **Click the backdrop** or the **×** button | Returns to the shelf |
| Remove one selection | **×** on a tray chip | That tape is deselected |
| Clear all selections | **Clear All** in the tray | The tray empties and slides away |

### 5.3 Selecting tapes

Click any tape to select it. The tray slides up from the bottom showing how many tapes you have
chosen and a colour-matched chip per tape. Selection is a toggle — clicking a selected tape
deselects it. There is no limit on how many tapes you can select at once.

### 5.4 Viewing tape details

Double-click a tape to open it. The detail view shows the tape case rendered large — brand strip,
title, subtitle, year, genre, rating, reels and window — alongside an information panel with the
runtime, rating badge and full synopsis. While a tape is open in detail view it no longer responds to
hover or selection clicks; close it first.

### 5.5 Things to know

- **Selections are not saved.** Reloading the page clears the tray — there is no persistence layer.
- **The catalogue is fixed.** Tapes are defined in the application source; they cannot be added,
  edited or removed from the interface. Adding a tape is a development task (see §2.5).
- **Double-click is required for details.** A single click only selects; this is deliberate so that
  selecting many tapes stays fast.
- **Best on a pointer device.** Hover feedback and double-click have no direct touch equivalents, so
  the experience is designed around mouse or trackpad.

---

## 6. Troubleshooting

| Symptom | Cause | Fix |
| --- | --- | --- |
| Dev server dies right after another one starts | Expected — the PID tracker kills the older instance | Run only one dev server; see §2.4 |
| Port 555 already in use | Another process holds the port (on some systems ports < 1024 also need privileges) | `npm run dev -- --port 5173` |
| Stale files in `web-portal/.pids` | Dev server was killed by a signal, so the `exit` hook never ran | `rm -rf .pids` — it is recreated on next start |
| `npm run lint` fails on a clean checkout | Three pre-existing errors on `master` | See §3.2 — fix the code or adjust the rules |
| `npm run build` fails before Vite runs | `tsc -b` found a type error | Fix the reported type error; the build gate is intentional |
| Deployed site loads blank, assets 404 | Hosted under a subpath without `base` configured | Set `base` in `vite.config.ts` — see §4.3 |
| Detail view animates oddly in dev but not in production | React StrictMode double-invocation affecting Framer Motion layout animations | Verify via `npm run preview` before concluding it is a real bug |

---

## 7. Command reference

```bash
npm install            # Install dependencies (npm ci is unavailable — lockfile is git-ignored)
npm run dev            # Dev server with HMR on http://localhost:555
npm run dev -- --host  # Same, exposed on the local network
npm run build          # Type-check (tsc -b) then build to dist/
npm run preview        # Serve the built dist/ locally
npm run lint           # ESLint over the repository
npx tsc -b             # Type-check only, no bundle
rm -rf .pids           # Reset PID tracking state
```
