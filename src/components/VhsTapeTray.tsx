import { motion, AnimatePresence } from "framer-motion";
import type { Tape } from "../data/tapes";
import type {TargetAndTransition} from "motion-dom";
import type {CSSProperties} from "react";


interface SelectedTrayProps {
  selectedTapes: Tape[];
  onRemove: (id: string) => void;
  onClear: () => void;
}

interface InitStateProps {
    tray: {
        base: TargetAndTransition;
        item: TargetAndTransition;
    };
}
const initState: InitStateProps = {
    tray: {
        base: { y: 100, opacity: 0 },
        item: { scale: 0, opacity: 0 }
    }
}

export default function VhsTapeTray({ selectedTapes, onRemove, onClear }: SelectedTrayProps) {
  if (selectedTapes.length === 0) return null;

  return (
    <motion.div
      className="selected-tray"
      initial={initState.tray.base}
      animate={{ y: 0, opacity: 1 }}
      exit={initState.tray.base}
    >
      <div className="tray-header">
        <span className="tray-count">{selectedTapes.length} tape{selectedTapes.length !== 1 ? "s" : ""} selected</span>
        <button className="tray-clear" onClick={onClear}>
          Clear All
        </button>
      </div>
      <div className="tray-items">
        <AnimatePresence>
          {selectedTapes.map((tape) => (
            <motion.div
              key={tape.id}
              className="tray-chip"
              style={{ "--tape-primary": tape.colors.primary } as CSSProperties}
              initial={initState.tray.item}
              animate={{ scale: 1, opacity: 1 }}
              exit={initState.tray.item}
              layout
            >
              <span>{tape.title}</span>
              <button className="chip-remove" onClick={() => onRemove(tape.id)}>
                &times;
              </button>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </motion.div>
  );
}
