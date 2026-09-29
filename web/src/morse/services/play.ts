import {
  DEFAULT_TONE_HZ,
  RAMP_SECONDS,
  START_DELAY_SECONDS,
  STOP_TAIL_SECONDS,
  VOLUME,
} from "@/morse/constants/audio";
import { DEFAULT_WPM } from "@/morse/constants/timing";
import type { PlayMorseOptions } from "@/morse/types/morse";
import { scheduleTones } from "@/morse/utils/schedule-tones";

/**
 * Plays Morse through Web Audio; returns a function that stops it early. `onEnd` runs once
 * either way. Gain automation runs on the audio clock, so timing is as exact as the app's
 * pre-rendered buffer (docs/08-audio-playback.md) without rendering samples.
 */
export const playMorse = (
  morse: string,
  { wpm = DEFAULT_WPM, toneHz = DEFAULT_TONE_HZ, onEnd }: PlayMorseOptions = {},
): (() => void) => {
  const schedule = scheduleTones(morse, wpm);
  const context = new AudioContext();
  const gain = context.createGain();
  const oscillator = context.createOscillator();
  oscillator.frequency.value = toneHz;
  oscillator.connect(gain).connect(context.destination);

  const t0 = context.currentTime + START_DELAY_SECONDS;
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
  oscillator.stop(t0 + schedule.duration + STOP_TAIL_SECONDS);

  let ended = false;
  const finish = () => {
    if (ended) return;
    ended = true;
    void context.close();
    onEnd?.();
  };
  oscillator.onended = finish;
  return finish;
};
