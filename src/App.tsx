import {useState, useCallback} from "react";
import {AnimatePresence, LayoutGroup} from "framer-motion";
import {tapes} from "./data/tapes";
import type {Tape} from "./data/tapes";
import ShelfVhsTape from "./components/ShelfVhsTape.tsx";
import VhsTapeDetail from "./components/VhsTapeDetail.tsx";
import VhsTapeTray from "./components/VhsTapeTray.tsx";
import "./App.css";

function App() {
    const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
    const [detailTape, setDetailTape] = useState<Tape | null>(null);

    const toggleSelect = useCallback(({id: tapeId}: Tape) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            next.has(tapeId) ? next.delete(tapeId) : next.add(tapeId);
            return next;
        });
    }, []);

    const handleDoubleClick = useCallback((tape: Tape) => {
        setDetailTape(tape);
    }, []);

    const removeSelected = useCallback((id: string) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            next.delete(id);
            return next;
        });
    }, []);

    const clearAll = useCallback(() => {
        setSelectedIds(new Set());
    }, []);

    const selectedTapes = tapes.filter((t) => selectedIds.has(t.id));

    return (
        <LayoutGroup>
            <div className="app">
                {/* Header */}
                <header className="app-header">
                    <div className="header-glow"/>
                    <h1 className="app-title">
                        <span className="title-accent">TAPE</span> GALLERY
                    </h1>
                    <p className="app-tagline">Select your tapes &bull; Double-click to preview</p>
                </header>

                {/* Tape Grid */}
                <main className="tape-grid">
                    {tapes.map((tape, i) => (
                        <div
                            key={tape.id}
                            onDoubleClick={() => handleDoubleClick(tape)}
                        >
                            <ShelfVhsTape
                                tape={tape}
                                isSelected={selectedIds.has(tape.id)}
                                isExpanded={detailTape?.id === tape.id}
                                onSelect={toggleSelect}
                                index={i}
                            />
                        </div>
                    ))}
                </main>

                {/* Detail Modal */}
                <VhsTapeDetail tape={detailTape} onClose={() => setDetailTape(null)}/>

                {/* Selected Tray */}
                <AnimatePresence>
                    {selectedTapes.length > 0 && (
                        <VhsTapeTray
                            selectedTapes={selectedTapes}
                            onRemove={removeSelected}
                            onClear={clearAll}
                        />
                    )}
                </AnimatePresence>
            </div>
        </LayoutGroup>
    );
}

export default App;
