import { CHAR_BY_CODE } from "@/morse/constants/alphabet";
import { REPLACEMENT_CHAR } from "@/morse/constants/notation";
import { PROSIGN_BY_CODE } from "@/morse/constants/prosigns";
import type { DecodeResult } from "@/morse/types/morse";
import { splitMorseWords } from "@/morse/utils/split-morse-words";

/**
 * `"... --- ..."` → `"SOS"`, like the app's `MorseCodec.decode`. A code that's no character but a
 * prosign is written `<SOS>`; anything else unreadable becomes the replacement character.
 */
export const decodeMorse = (morse: string): DecodeResult => {
  const unreadable = new Set<string>();
  const text = splitMorseWords(morse)
    .map((tokens) =>
      tokens
        .map((token) => {
          const char = CHAR_BY_CODE.get(token);
          if (char !== undefined) return char;
          const prosign = PROSIGN_BY_CODE.get(token);
          if (prosign !== undefined) return `<${prosign}>`;
          unreadable.add(token);
          return REPLACEMENT_CHAR;
        })
        .join(""),
    )
    .join(" ");
  return { text, unreadable: [...unreadable] };
};
