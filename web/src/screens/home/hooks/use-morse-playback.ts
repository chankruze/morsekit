import { useCallback, useEffect, useRef, useState } from "react";
import { playMorse } from "@/morse/services/play";

/** Like the app, playback stops when [resetKey] changes (editing, swapping) or on unmount. */
export const useMorsePlayback = (morse: string, resetKey: string) => {
  const [playing, setPlaying] = useState(false);
  const stopRef = useRef<(() => void) | null>(null);

  const stop = useCallback(() => {
    stopRef.current?.();
    stopRef.current = null;
  }, []);

  useEffect(() => stop, [resetKey, stop]);

  const toggle = useCallback(() => {
    if (stopRef.current) return stop();
    setPlaying(true);
    stopRef.current = playMorse(morse, {
      onEnd: () => {
        stopRef.current = null;
        setPlaying(false);
      },
    });
  }, [morse, stop]);

  return { playing, toggle, canPlay: playing || morse.trim().length > 0 };
};
