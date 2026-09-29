import { useEffect, useState } from "react";
import { SIGNAL_TICK_MS } from "@/screens/home/constants/morse-signal";

const MS_PER_SECOND = 1000;
const REDUCED_MOTION = "(prefers-reduced-motion: reduce)";

/** Seconds into the current loop, or `null` when the user prefers reduced motion. */
export const useSignalClock = (loopSeconds: number): number | null => {
  const [now, setNow] = useState<number | null>(null);

  useEffect(() => {
    if (window.matchMedia(REDUCED_MOTION).matches) return;
    const start = performance.now();
    const id = setInterval(
      () => setNow(((performance.now() - start) / MS_PER_SECOND) % loopSeconds),
      SIGNAL_TICK_MS,
    );
    return () => clearInterval(id);
  }, [loopSeconds]);

  return now;
};
