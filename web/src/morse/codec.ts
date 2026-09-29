import { ALPHABET, CHAR_BY_CODE } from "./alphabet";

/** Mirrors the app's `MorseCodec`, `MorseNormalizer` and `MorseTokenizer`. */

export const WORD_SEPARATOR = " / ";
export const REPLACEMENT_CHAR = "�";

const ALTERNATIVE_DOTS = new Set(["·", "•", "∙", "⋅"]);
const ALTERNATIVE_DASHES = new Set(["−", "–", "—", "‒", "_"]);
const SINGLE_QUOTES = new Set(["‘", "’", "‚", "′"]);
const DOUBLE_QUOTES = new Set(["“", "”", "„", "″"]);
const MORSE_WORD_BREAKS = new Set(["/", "|", "\n", "\r"]);

export interface EncodeResult {
  /** Canonical notation, e.g. `... --- ...`. */
  morse: string;
  /** Characters with no Morse code; they're left out of [morse]. */
  unsupported: string[];
}

export interface DecodeResult {
  text: string;
  /** Tokens that aren't a known letter; each became [REPLACEMENT_CHAR]. */
  unreadable: string[];
}

function normalizeTextChar(char: string): string {
  if (char >= "a" && char <= "z") return char.toUpperCase();
  if (SINGLE_QUOTES.has(char)) return "'";
  if (DOUBLE_QUOTES.has(char)) return '"';
  return char;
}

function normalizeMorseChar(char: string): string {
  if (ALTERNATIVE_DOTS.has(char)) return ".";
  if (ALTERNATIVE_DASHES.has(char)) return "-";
  return char;
}

/** `"SOS"` → `"... --- ..."`. Unsupported characters are skipped and reported. */
export function encode(text: string): EncodeResult {
  const unsupported = new Set<string>();
  const words = text
    .split(/\s+/u)
    .map((word) =>
      // Array.from iterates by code point, so an emoji is one symbol, as in the app.
      Array.from(word).flatMap((symbol) => {
        const code = ALPHABET[normalizeTextChar(symbol)];
        if (code === undefined) {
          unsupported.add(symbol);
          return [];
        }
        return [code];
      }),
    )
    .filter((letters) => letters.length > 0);
  return {
    morse: words.map((letters) => letters.join(" ")).join(WORD_SEPARATOR),
    unsupported: [...unsupported],
  };
}

/**
 * Splits Morse into words of letter tokens. Words break at `/`, `|`, a newline, or two or
 * more spaces; letters at a single space.
 */
export function morseWords(morse: string): string[][] {
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
      if (++whitespaceRun === 2) endWord();
    } else {
      whitespaceRun = 0;
      token += char;
    }
  }
  endWord();
  return words;
}

/** `"... --- ..."` → `"SOS"`. Unreadable letters become [REPLACEMENT_CHAR] and are reported. */
export function decode(morse: string): DecodeResult {
  const unreadable = new Set<string>();
  const text = morseWords(morse)
    .map((tokens) =>
      tokens
        .map((token) => {
          const char = CHAR_BY_CODE.get(token);
          if (char === undefined) unreadable.add(token);
          return char ?? REPLACEMENT_CHAR;
        })
        .join(""),
    )
    .join(" ");
  return { text, unreadable: [...unreadable] };
}

/** `.-` → `•−`, the glyphs the app shows on screen. */
export function toDisplayGlyphs(morse: string): string {
  return morse.replaceAll(".", "•").replaceAll("-", "−");
}
