import { DOT } from "@/morse/constants/notation";
import {
  DASH_UNITS,
  DEFAULT_WPM,
  DOT_UNITS,
  ELEMENT_GAP_UNITS,
  LETTER_GAP_UNITS,
  SECONDS_PER_UNIT_AT_1_WPM,
  WORD_GAP_UNITS,
} from "@/morse/constants/timing";
import type { Tone, ToneSchedule } from "@/morse/types/morse";
import { splitMorseWords } from "@/morse/utils/split-morse-words";

const WELL_FORMED_LETTER = /^[.-]+$/;

export const unitSeconds = (wpm: number): number =>
  SECONDS_PER_UNIT_AT_1_WPM / wpm;

/** Malformed tokens are skipped, as in the app's `MorseCodec.parse`. */
export const scheduleTones = (
  morse: string,
  wpm: number = DEFAULT_WPM,
): ToneSchedule => {
  const unit = unitSeconds(wpm);
  const tones: Tone[] = [];
  let units = 0;
  let pendingGap = 0;
  for (const word of splitMorseWords(morse)) {
    const letters = word.filter((token) => WELL_FORMED_LETTER.test(token));
    if (!letters.length) continue;
    if (tones.length) pendingGap = WORD_GAP_UNITS;
    letters.forEach((letter, i) => {
      if (i > 0) pendingGap = LETTER_GAP_UNITS;
      [...letter].forEach((element, j) => {
        if (j > 0) pendingGap = ELEMENT_GAP_UNITS;
        units += pendingGap;
        pendingGap = 0;
        const length = element === DOT ? DOT_UNITS : DASH_UNITS;
        tones.push({ start: units * unit, duration: length * unit });
        units += length;
      });
    });
  }
  return { tones, duration: units * unit };
};
