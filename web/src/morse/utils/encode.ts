import { ALPHABET } from "@/morse/constants/alphabet";
import { LETTER_SEPARATOR, WORD_SEPARATOR } from "@/morse/constants/notation";
import { PROSIGN_CODE_BY_LETTERS } from "@/morse/constants/prosigns";
import type { EncodeResult } from "@/morse/types/morse";
import { normalizeTextChar } from "@/morse/utils/normalize-char";

/** The code of a prosign written in angle brackets at the start of [text] (`<SOS>…`), and its length. */
const leadingProsign = (
  text: string,
): { code: string; length: number } | null => {
  if (!text.startsWith("<")) return null;
  const end = text.indexOf(">");
  if (end < 0) return null;
  const code = PROSIGN_CODE_BY_LETTERS.get(text.slice(1, end).toUpperCase());
  return code ? { code, length: end + 1 } : null;
};

/**
 * `"SOS"` → `"... --- ..."`, like the app's `MorseCodec.encode`. `<SOS>` becomes one letter, its
 * letters run together (`...---...`).
 */
export const encodeText = (text: string): EncodeResult => {
  const unsupported = new Set<string>();
  const words = text
    .split(/\s+/u)
    .map((word) => {
      const letters: string[] = [];
      let rest = word;
      while (rest) {
        const prosign = leadingProsign(rest);
        if (prosign) {
          letters.push(prosign.code);
          rest = rest.slice(prosign.length);
          continue;
        }
        // One code point at a time, so an emoji counts as one symbol, as in the app.
        const symbol = String.fromCodePoint(rest.codePointAt(0)!);
        const code = ALPHABET[normalizeTextChar(symbol)];
        if (code === undefined) unsupported.add(symbol);
        else letters.push(code);
        rest = rest.slice(symbol.length);
      }
      return letters;
    })
    .filter((letters) => letters.length > 0);
  return {
    morse: words
      .map((letters) => letters.join(LETTER_SEPARATOR))
      .join(WORD_SEPARATOR),
    unsupported: [...unsupported],
  };
};
