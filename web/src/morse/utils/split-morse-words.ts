import {
  MORSE_WORD_BREAKS,
  WORD_BREAK_SPACES,
} from "@/morse/constants/notation";
import { normalizeMorseChar } from "@/morse/utils/normalize-char";

/** Mirrors the app's `MorseTokenizer.morseWords`. */
export const splitMorseWords = (morse: string): string[][] => {
  const words: string[][] = [];
  let letters: string[] = [];
  let token = "";
  let whitespaceRun = 0;
  const endToken = () => {
    if (token) letters.push(token);
    token = "";
  };
  const endWord = () => {
    endToken();
    if (letters.length) words.push(letters);
    letters = [];
  };
  for (const raw of morse) {
    const char = normalizeMorseChar(raw);
    if (MORSE_WORD_BREAKS.has(char)) {
      endWord();
      whitespaceRun = 0;
    } else if (/\s/u.test(char)) {
      endToken();
      if (++whitespaceRun === WORD_BREAK_SPACES) endWord();
    } else {
      whitespaceRun = 0;
      token += char;
    }
  }
  endWord();
  return words;
};
