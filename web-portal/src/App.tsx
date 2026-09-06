import {useState, useCallback, useEffect} from "react";
import {AnimatePresence, LayoutGroup} from "framer-motion";
import {fetchTapes} from "./api/tapes";
import type {Tape} from "./data/tapes";
import ShelfVhsTape from "./components/ShelfVhsTape.tsx";
import VhsTapeDetail from "./components/VhsTapeDetail.tsx";
import VhsTapeTray from "./components/VhsTapeTray.tsx";
import "./App.css";

function App() {
    const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
    const [detailTape, setDetailTape] = useState<Tape | null>(null);
    const [tapes, setTapes] = useState<Tape[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // StrictMode double-invokes effects in dev, so the teardown aborts the
        // first request rather than letting it resolve into unmounted state.
        const controller = new AbortController();

        fetchTapes(controller.signal)
            .then((loaded: Tape[]) => {
                setTapes(loaded);
                setError(null);
            })
            .catch((cause: unknown) => {
                if (cause instanceof DOMException && cause.name === "AbortError") {
                    return;
                }
                setError(cause instanceof Error ? cause.message : "Could not load tapes.");
            })
            .finally(() => {
                if (!controller.signal.aborted) {
                    setIsLoading(false);
                }
            });

        return () => controller.abort();
    }, []);

    const toggleSelect = useCallback(({id: tapeId}: Tape) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            if (next.has(tapeId)) {
                next.delete(tapeId);
            } else {
                next.add(tapeId);
            }
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
                {isLoading ? (
                    <main className="shelf-status">
                        <p className="shelf-status-message">Loading tapes&hellip;</p>
                    </main>
                ) : error ? (
                    <main className="shelf-status">
                        <p className="shelf-status-message shelf-status-error">{error}</p>
                        <p className="shelf-status-hint">
                            Is tapes-hub running on port 8080 with the <code>dev</code> profile?
                        </p>
                    </main>
                ) : tapes.length === 0 ? (
                    <main className="shelf-status">
                        <p className="shelf-status-message">The shelf is empty.</p>
                    </main>
                ) : (
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
                )}

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
