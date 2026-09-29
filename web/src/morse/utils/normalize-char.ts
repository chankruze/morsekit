import {
  ALTERNATIVE_DASHES,
  ALTERNATIVE_DOTS,
  DASH,
  DOT,
  DOUBLE_QUOTES,
  SINGLE_QUOTES,
} from "@/morse/constants/notation";

/** Mirrors the app's `MorseNormalizer.normalizeTextChar`. */
export const normalizeTextChar = (char: string): string => {
  if (char >= "a" && char <= "z") return char.toUpperCase();
  if (SINGLE_QUOTES.has(char)) return "'";
  if (DOUBLE_QUOTES.has(char)) return '"';
  return char;
};

/** Mirrors the app's `MorseNormalizer.normalizeMorseChar`. */
export const normalizeMorseChar = (char: string): string => {
  if (ALTERNATIVE_DOTS.has(char)) return DOT;
  if (ALTERNATIVE_DASHES.has(char)) return DASH;
  return char;
};
