import { ALPHABET } from "@/morse/constants/alphabet";
import { LETTER_SEPARATOR, WORD_SEPARATOR } from "@/morse/constants/notation";
import type { EncodeResult } from "@/morse/types/morse";
import { normalizeTextChar } from "@/morse/utils/normalize-char";

/** `"SOS"` → `"... --- ..."`, like the app's `MorseCodec.encode`. */
export const encodeText = (text: string): EncodeResult => {
  const unsupported = new Set<string>();
  const words = text
    .split(/\s+/u)
    .map((word) =>
      // Array.from iterates by code point, so an emoji counts as one symbol, as in the app.
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
    morse: words
      .map((letters) => letters.join(LETTER_SEPARATOR))
      .join(WORD_SEPARATOR),
    unsupported: [...unsupported],
  };
};
