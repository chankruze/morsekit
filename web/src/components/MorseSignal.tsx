import { useEffect, useState } from "react";
import { encode } from "../morse/codec";
import { scheduleTones } from "../morse/audio";

const WORD = "MORSEKIT";
const WPM = 10;
const PAUSE_SECONDS = 1.2;

const morse = encode(WORD).morse;
const { tones, duration } = scheduleTones(morse, WPM);
const loop = duration + PAUSE_SECONDS;

/** "MORSEKIT" in Morse, lit element by element with real 10 WPM timing, like the flashlight. */
export function MorseSignal() {
  const [now, setNow] = useState(-1);

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const start = performance.now();
    const id = setInterval(() => setNow(((performance.now() - start) / 1000) % loop), 30);
    return () => clearInterval(id);
  }, []);

  let element = 0;
  return (
    <div className="flex flex-wrap items-center justify-center gap-x-5 gap-y-3" aria-hidden="true">
      {morse.split(" ").map((letter, i) => (
        <div key={i} className="flex items-center gap-1.5">
          {[...letter].map((mark) => {
            const index = element++;
            const tone = tones[index];
            const lit = now < 0 || (now >= tone.start && now < tone.start + tone.duration);
            const colour =
              mark === "."
                ? index % 2 ? "bg-ember shadow-ember/60" : "bg-sun shadow-sun/60"
                : index % 2 ? "bg-cyan shadow-cyan/60" : "bg-cream shadow-cream/50";
            return (
              <span
                key={index}
                className={`h-3 rounded-full transition-[opacity,box-shadow] duration-75 ${
                  mark === "." ? "w-3" : "w-9"
                } ${colour} ${lit ? "opacity-100 shadow-[0_0_14px]" : "opacity-20"}`}
              />
            );
          })}
        </div>
      ))}
    </div>
  );
}
