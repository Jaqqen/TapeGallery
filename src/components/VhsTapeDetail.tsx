import {motion, AnimatePresence } from "framer-motion";
import type {Tape} from "../data/tapes";
import type {CSSProperties} from "react";
import type {TargetAndTransition} from "motion-dom";

interface TapeDetailProps {
    tape: Tape | null;
    onClose: () => void;
}

interface InitStateProps {
    detail: {
        content: TargetAndTransition;
        overlay: TargetAndTransition;
        info: {
            panel: TargetAndTransition;
        };
    };
}
const initState: InitStateProps = {
    detail: {
        content: {rotateY: "270deg"},
        overlay: {opacity: 0},
        info: {
            panel: {opacity: 0, y: 30}
        }
    }
}


export default function VhsTapeDetail({tape, onClose}: TapeDetailProps) {
    if (!tape?.id) return null;

    const tapeId: Tape["id"] = tape.id;

    return (
        <AnimatePresence>
            <motion.div
                key={tapeId}
                className="tape-detail-overlay"
                initial={initState.detail.overlay}
                animate={{opacity: 1}}
                exit={initState.detail.overlay}
                transition={{duration: 2}}
                onClick={onClose}
                style={{
                    perspective: '800px'
                }}
            >
                <div className="tape-detail-content" onClick={(e) => e.stopPropagation()}>
                    {/* The tape case that animates from the grid */}
                    {/* The colors are dynamically assigned to the variables (e.g. '--tape-primary') which are then set
                        in the CSS files through declaring them as CSSProperties */}
                    <motion.div
                        initial={initState.detail.content}
                        animate={{rotateY: "0deg"}}
                        exit={initState.detail.content}
                        className="detail-tape-case"
                        layoutId={`tape-case-${tapeId}`}
                        style={{
                            "--tape-primary": tape.colors.primary,
                            "--tape-secondary": tape.colors.secondary,
                            "--tape-accent": tape.colors.accent,
                            "--tape-label": tape.colors.label,
                            transformStyle: "preserve-3d"
                        } as CSSProperties}
                        transition={{duration: 2}}
                    >
                        <motion.div className="tape-top-edge" layoutId={`tape-edge-${tapeId}`}/>

                        <motion.div className="tape-label detail-expanded-label" layoutId={`tape-label-${tapeId}`}>
                            <div className="label-content">
                                <div className="tape-brand-strip">
                                    <span className="brand-logo">VHS</span>
                                    <span className="brand-hifi">Hi-Fi STEREO</span>
                                </div>
                                <div className="tape-title-block">
                                    <motion.h3 className="tape-title detail-tape-title"
                                               layoutId={`tape-title-${tapeId}`}>
                                        {tape.title}
                                    </motion.h3>
                                    {tape.subtitle && <p className="tape-subtitle">{tape.subtitle}</p>}
                                </div>
                                <div className="tape-info-strip">
                                    <span className="tape-year">{tape.year}</span>
                                    <span className="tape-genre">{tape.genre}</span>
                                    <span className="tape-rating">{tape.rating}</span>
                                </div>
                            </div>
                        </motion.div>

                        <motion.div className="tape-window" layoutId={`tape-window-${tapeId}`}>
                            <div className="reel left-reel">
                                <div className="reel-teeth"/>
                            </div>
                            <div className="tape-strip"/>
                            <div className="reel right-reel">
                                <div className="reel-teeth"/>
                            </div>
                        </motion.div>
                    </motion.div>

                    {/* Info panel that fades in after tape arrives */}
                    <motion.div
                        className="detail-info-panel"
                        initial={initState.detail.info.panel}
                        animate={{opacity: 1, y: 0}}
                        exit={initState.detail.info.panel}
                        transition={{delay: 0.25, duration: 0.4, ease: "easeOut"}}
                        style={{
                            "--tape-primary": tape.colors.primary,
                            "--tape-secondary": tape.colors.secondary,
                            "--tape-accent": tape.colors.accent,
                        } as CSSProperties}
                    >
                        <button className="detail-close" onClick={onClose}>
                            &times;
                        </button>

                        <div className="detail-header">
                            <div className="detail-rating-badge">{tape.rating}</div>
                            <div className="detail-meta">
                                <span>{tape.year}</span>
                                <span className="meta-dot"/>
                                <span>{tape.genre}</span>
                                <span className="meta-dot"/>
                                <span>{tape.duration}</span>
                            </div>
                        </div>

                        <h2 className="detail-title">{tape.title}</h2>
                        {tape.subtitle && <p className="detail-subtitle">{tape.subtitle}</p>}

                        <p className="detail-description">{tape.description}</p>

                        <div className="detail-actions">
                            <button className="detail-btn detail-btn-play">
                                <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                                    <polygon points="5 3 19 12 5 21 5 3"/>
                                </svg>
                                Play
                            </button>
                            <button className="detail-btn detail-btn-secondary">Add to Collection</button>
                        </div>
                    </motion.div>
                </div>
            </motion.div>
        </AnimatePresence>
    );
}
