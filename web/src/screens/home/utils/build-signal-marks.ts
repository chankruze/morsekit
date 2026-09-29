import { DOT, LETTER_SEPARATOR } from "@/morse/constants/notation";
import { encodeText } from "@/morse/utils/encode";
import { scheduleTones } from "@/morse/utils/schedule-tones";
import {
  DASH_COLOURS,
  DOT_COLOURS,
} from "@/screens/home/constants/morse-signal";
import type { SignalMark } from "@/screens/home/types/home";

export type SignalLetters = {
  letters: SignalMark[][];
  loopSeconds: number;
};

/** Splits a word's Morse into letters of marks, each with the tone that lights it. */
export const buildSignalMarks = (
  word: string,
  wpm: number,
  pauseSeconds: number,
): SignalLetters => {
  const morse = encodeText(word).morse;
  const { tones, duration } = scheduleTones(morse, wpm);
  let id = 0;
  const letters = morse.split(LETTER_SEPARATOR).map((letter) =>
    [...letter].map((element): SignalMark => {
      const isDot = element === DOT;
      const colours = isDot ? DOT_COLOURS : DASH_COLOURS;
      const mark = {
        id,
        isDot,
        tone: tones[id],
        colourClass: colours[id % colours.length],
      };
      id += 1;
      return mark;
    }),
  );
  return { letters, loopSeconds: duration + pauseSeconds };
};
