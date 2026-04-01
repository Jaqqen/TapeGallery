import { motion } from "framer-motion";
import type { Tape } from "../data/tapes";
import type {CSSProperties} from "react";


interface VHSTapeProps {
  tape: Tape;
  isSelected: boolean;
  isExpanded: boolean;
  onSelect: (tape: Tape) => void;
  index: number;
}

function TapePattern({ pattern, colors }: { pattern: Tape["pattern"]; colors: Tape["colors"] }) {
  switch (pattern) {
    case "stripes":
      return (
        <svg width="100%" height="100%" className="tape-pattern">
          {Array.from({ length: 8 }).map((_, i) => (
            <rect
              key={i}
              x="0"
              y={i * 12.5 + "%"}
              width="100%"
              height="6%"
              fill={i % 2 === 0 ? colors.primary : colors.secondary}
              opacity={0.3}
            />
          ))}
        </svg>
      );
    case "gradient":
      return (
        <svg width="100%" height="100%" className="tape-pattern">
          <defs>
            <linearGradient id={`grad-${colors.primary}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={colors.primary} stopOpacity="0.4" />
              <stop offset="100%" stopColor={colors.secondary} stopOpacity="0.4" />
            </linearGradient>
          </defs>
          <rect width="100%" height="100%" fill={`url(#grad-${colors.primary})`} />
        </svg>
      );
    case "geometric":
      return (
        <svg width="100%" height="100%" className="tape-pattern">
          {Array.from({ length: 4 }).map((_, i) => (
            <circle
              key={i}
              cx={25 + i * 20 + "%"}
              cy="50%"
              r="18%"
              fill="none"
              stroke={colors.primary}
              strokeWidth="2"
              opacity="0.3"
            />
          ))}
        </svg>
      );
    case "retro-blocks":
      return (
        <svg width="100%" height="100%" className="tape-pattern">
          {Array.from({ length: 6 }).map((_, i) => (
            <rect
              key={i}
              x={i * 16.6 + "%"}
              y="10%"
              width="14%"
              height="80%"
              rx="2"
              fill={i % 3 === 0 ? colors.primary : i % 3 === 1 ? colors.secondary : colors.accent}
              opacity="0.25"
            />
          ))}
        </svg>
      );
    case "waves":
      return (
        <svg width="100%" height="100%" className="tape-pattern" viewBox="0 0 200 100" preserveAspectRatio="none">
          <path
            d="M0,50 Q25,20 50,50 T100,50 T150,50 T200,50"
            fill="none"
            stroke={colors.primary}
            strokeWidth="3"
            opacity="0.3"
          />
          <path
            d="M0,65 Q25,35 50,65 T100,65 T150,65 T200,65"
            fill="none"
            stroke={colors.secondary}
            strokeWidth="3"
            opacity="0.3"
          />
        </svg>
      );
    case "diamonds":
      return (
        <svg width="100%" height="100%" className="tape-pattern" viewBox="0 0 200 100" preserveAspectRatio="none">
          {Array.from({ length: 5 }).map((_, i) => (
            <polygon
              key={i}
              points={`${20 + i * 40},10 ${40 + i * 40},50 ${20 + i * 40},90 ${i * 40},50`}
              fill={colors.primary}
              opacity="0.2"
            />
          ))}
        </svg>
      );
  }
}

export default function ShelfVhsTape({ tape, isSelected, isExpanded, onSelect, index }: VHSTapeProps) {
  return (
    <motion.div
      className={`vhs-tape ${isSelected ? "selected" : ""} ${isExpanded ? "expanded" : ""}`}
      style={{
        "--tape-primary": tape.colors.primary,
        "--tape-secondary": tape.colors.secondary,
        "--tape-accent": tape.colors.accent,
        "--tape-label": tape.colors.label,
      } as CSSProperties}
      initial={{ opacity: 0, y: 40 }}
      animate={{ opacity: isExpanded ? 0 : 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.06 }}
      whileHover={isExpanded ? undefined : { y: -8, scale: 1.03 }}
      whileTap={isExpanded ? undefined : { scale: 0.97 }}
      onClick={() => !isExpanded && onSelect(tape)}
    >
      {/* Tape case body */}
      <motion.div className="tape-case" layoutId={`tape-case-${tape.id}`}>
        {/* Top edge */}
        <motion.div className="tape-top-edge" layoutId={`tape-edge-${tape.id}`} />

        {/* Label area */}
        <motion.div className="tape-label" layoutId={`tape-label-${tape.id}`}>
          <TapePattern pattern={tape.pattern} colors={tape.colors} />

          <div className="label-content">
            {/* Brand strip */}
            <div className="tape-brand-strip">
              <span className="brand-logo">VHS</span>
              <span className="brand-hifi">Hi-Fi STEREO</span>
            </div>

            {/* Title block */}
            <div className="tape-title-block">
              <motion.h3 className="tape-title" layoutId={`tape-title-${tape.id}`}>
                {tape.title}
              </motion.h3>
              {tape.subtitle && <p className="tape-subtitle">{tape.subtitle}</p>}
            </div>

            {/* Info strip */}
            <div className="tape-info-strip">
              <span className="tape-year">{tape.year}</span>
              <span className="tape-genre">{tape.genre}</span>
              <span className="tape-rating">{tape.rating}</span>
            </div>
          </div>

          {/* Selection checkmark overlay */}
          {isSelected && !isExpanded && (
            <motion.div
              className="tape-selected-badge"
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: "spring", stiffness: 500, damping: 25 }}
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </motion.div>
          )}
        </motion.div>

        {/* Bottom window (reel holes) */}
        <motion.div className="tape-window" layoutId={`tape-window-${tape.id}`}>
          <div className="reel left-reel">
            <div className="reel-teeth" />
          </div>
          <div className="tape-strip" />
          <div className="reel right-reel">
            <div className="reel-teeth" />
          </div>
        </motion.div>

        {/* Bottom edge */}
        <div className="tape-bottom-edge">
          <span className="tape-duration">{tape.duration}</span>
        </div>
      </motion.div>
    </motion.div>
  );
}
