/**
 * Morse timing and Web Audio playback, following the app's audio design
 * (docs/08-audio-playback.md): standard PARIS timing, a 600 Hz tone and 5 ms fades so tones
 * don't click. The schedule is pure and tested; `playMorse` only hands it to the browser.
 */
import { morseWords } from "./codec";

export const DEFAULT_WPM = 20;
export const DEFAULT_TONE_HZ = 600;
const RAMP_SECONDS = 0.005;
const VOLUME = 0.25;

export interface Tone {
  /** Seconds from the start. */
  start: number;
  duration: number;
}

export interface ToneSchedule {
  tones: Tone[];
  /** Seconds, up to the end of the last tone. */
  duration: number;
}

/** One unit, the length of a dot: 1.2 s / WPM (PARIS standard). */
export const unitSeconds = (wpm: number) => 1.2 / wpm;

/**
 * Dot 1 unit, dash 3; gaps of 1 inside a letter, 3 between letters, 7 between words.
 * Malformed tokens (anything but dots and dashes) are skipped, as in the app's `parse()`.
 */
export function scheduleTones(morse: string, wpm = DEFAULT_WPM): ToneSchedule {
  const unit = unitSeconds(wpm);
  const tones: Tone[] = [];
  let units = 0;
  let pendingGap = 0;
  for (const word of morseWords(morse)) {
    const letters = word.filter((token) => /^[.-]+$/.test(token));
    if (!letters.length) continue;
    if (tones.length) pendingGap = 7;
    letters.forEach((letter, i) => {
      if (i > 0) pendingGap = 3;
      for (const [j, element] of [...letter].entries()) {
        if (j > 0) pendingGap = 1;
        units += pendingGap;
        pendingGap = 0;
        const length = element === "." ? 1 : 3;
        tones.push({ start: units * unit, duration: length * unit });
        units += length;
      }
    });
  }
  return { tones, duration: units * unit };
}

/** Plays [morse]; call the returned function to stop early. [onEnd] runs once either way. */
export function playMorse(
  morse: string,
  { wpm = DEFAULT_WPM, toneHz = DEFAULT_TONE_HZ, onEnd = () => {} } = {},
): () => void {
  const schedule = scheduleTones(morse, wpm);
  const context = new AudioContext();
  const gain = context.createGain();
  const oscillator = context.createOscillator();
  oscillator.frequency.value = toneHz;
  oscillator.connect(gain).connect(context.destination);

  const t0 = context.currentTime + 0.1; // like the app's padding: start-up latency can't eat the first dot
  gain.gain.setValueAtTime(0, t0);
  for (const { start, duration } of schedule.tones) {
    const on = t0 + start;
    const off = on + duration;
    gain.gain.setValueAtTime(0, on);
    gain.gain.linearRampToValueAtTime(VOLUME, on + RAMP_SECONDS);
    gain.gain.setValueAtTime(VOLUME, off - RAMP_SECONDS);
    gain.gain.linearRampToValueAtTime(0, off);
  }
  oscillator.start(t0);
  oscillator.stop(t0 + schedule.duration + 0.05);

  let ended = false;
  const finish = () => {
    if (ended) return;
    ended = true;
    void context.close();
    onEnd();
  };
  oscillator.onended = finish;
  return finish;
}
