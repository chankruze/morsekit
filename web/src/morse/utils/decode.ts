import { CHAR_BY_CODE } from "@/morse/constants/alphabet";
import { REPLACEMENT_CHAR } from "@/morse/constants/notation";
import type { DecodeResult } from "@/morse/types/morse";
import { splitMorseWords } from "@/morse/utils/split-morse-words";

/** `"... --- ..."` → `"SOS"`, like the app's `MorseCodec.decode`. */
export const decodeMorse = (morse: string): DecodeResult => {
  const unreadable = new Set<string>();
  const text = splitMorseWords(morse)
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
};
