import { useEffect, useRef, useState } from "react";
import { playMorse } from "../morse/audio";
import { decode, encode, toDisplayGlyphs } from "../morse/codec";
import { Icon } from "./icons";

type Direction = "text" | "morse";

const PLACEHOLDER: Record<Direction, string> = {
  text: "Type a message…",
  morse: "Type dots and dashes, e.g. ... --- ...",
};

/** A small version of the app's translator: live conversion, swap, copy and sound. */
export function Translator() {
  const [direction, setDirection] = useState<Direction>("text");
  const [input, setInput] = useState("Hello world");
  const [playing, setPlaying] = useState(false);
  const [copied, setCopied] = useState(false);
  const stop = useRef<(() => void) | null>(null);

  const encoded = direction === "text" ? encode(input) : null;
  const decoded = direction === "morse" ? decode(input) : null;
  const output = encoded ? encoded.morse : decoded!.text;
  const morseToPlay = encoded ? encoded.morse : input;
  const skipped = encoded ? encoded.unsupported : decoded!.unreadable;

  const stopPlaying = () => {
    stop.current?.();
    stop.current = null;
  };

  // Like the app: editing, swapping or leaving stops the sound.
  useEffect(() => stopPlaying, [input, direction]);

  const togglePlay = () => {
    if (playing) return stopPlaying();
    setPlaying(true);
    stop.current = playMorse(morseToPlay, { onEnd: () => setPlaying(false) });
  };

  const swap = () => {
    setInput(output);
    setDirection(direction === "text" ? "morse" : "text");
  };

  const copy = async () => {
    await navigator.clipboard.writeText(output);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  const label = (d: Direction) => (d === "text" ? "Text" : "Morse");
  const other: Direction = direction === "text" ? "morse" : "text";

  return (
    <div className="rounded-3xl border border-white/10 bg-navy/60 p-4 shadow-2xl shadow-black/40 backdrop-blur sm:p-6">
      <div className="mb-4 flex items-center justify-between gap-3">
        <span className="rounded-full bg-white/10 px-4 py-1.5 text-sm font-medium">{label(direction)}</span>
        <button
          type="button"
          onClick={swap}
          className="rounded-full p-2 text-cyan transition hover:bg-white/10"
          aria-label={`Swap: translate ${label(other)} to ${label(direction)}`}
        >
          <Icon name="translate" className="size-6" />
        </button>
        <span className="rounded-full bg-white/10 px-4 py-1.5 text-sm font-medium">{label(other)}</span>
      </div>

      <label className="block">
        <span className="sr-only">{label(direction)} to translate</span>
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={PLACEHOLDER[direction]}
          rows={3}
          spellCheck={direction === "text"}
          className="w-full resize-none rounded-2xl bg-white/5 p-4 text-lg placeholder:text-cream/40 focus:bg-white/10 focus:outline-none"
        />
      </label>

      <div className="mt-3 rounded-2xl bg-brand/30 p-4" aria-live="polite">
        <p className="text-xs font-medium tracking-wider text-cyan uppercase">{label(other)}</p>
        <p
          className={`mt-1 min-h-14 break-words ${
            direction === "text" ? "text-2xl tracking-widest" : "text-xl"
          } ${output ? "" : "text-cream/40"}`}
        >
          {output ? (direction === "text" ? toDisplayGlyphs(output) : output) : "Translation appears here"}
        </p>
        {skipped.length > 0 && (
          <p className="mt-2 text-sm text-sun">
            {direction === "text" ? "No Morse for: " : "Couldn't read: "}
            {skipped.join(" ")}
          </p>
        )}
        <div className="mt-3 flex justify-end gap-2">
          <button
            type="button"
            onClick={copy}
            disabled={!output}
            className="flex items-center gap-2 rounded-full px-4 py-2 text-sm transition hover:bg-white/10 disabled:opacity-40"
          >
            <Icon name="copy" className="size-4" />
            {copied ? "Copied" : "Copy"}
          </button>
          <button
            type="button"
            onClick={togglePlay}
            disabled={!playing && !morseToPlay.trim()}
            className={`flex items-center gap-2 rounded-full px-5 py-2 text-sm font-semibold transition disabled:opacity-40 ${
              playing ? "bg-ember text-night" : "bg-cyan text-night hover:bg-cyan/85"
            }`}
          >
            <Icon name={playing ? "stop" : "volume"} className="size-4" />
            {playing ? "Stop" : "Play sound"}
          </button>
        </div>
      </div>
    </div>
  );
}
