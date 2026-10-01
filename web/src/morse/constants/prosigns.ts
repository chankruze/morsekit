import { ALPHABET, CHAR_BY_CODE } from "@/morse/constants/alphabet";

/**
 * The prosigns the app knows (`MorseProsigns.Common` in
 * shared/src/commonMain/kotlin/in/geekofia/morsekit/core/morse/MorseProsigns.kt); `prosigns.test.ts`
 * fails if the two lists differ. A prosign is its letters sent as one character, no gaps.
 */
export const PROSIGN_LETTERS: readonly string[] = [
  "SOS",
  "AR",
  "SK",
  "BT",
  "KN",
  "AS",
  "CT",
  "VE",
  "HH",
  "CL",
];

/** `SOS` → `...---...`: the letters' codes run together, so they can't disagree with the alphabet. */
export const PROSIGN_CODE_BY_LETTERS: ReadonlyMap<string, string> = new Map(
  PROSIGN_LETTERS.map((letters) => [
    letters,
    [...letters].map((c) => ALPHABET[c]).join(""),
  ]),
);

/**
 * `...---...` → `SOS`, for decoding. Prosigns whose code is also a character (AR is `+`, BT `=`,
 * KN `(`, AS `&`) are left out: the alphabet wins, as in the app.
 */
export const PROSIGN_BY_CODE: ReadonlyMap<string, string> = new Map(
  [...PROSIGN_CODE_BY_LETTERS]
    .filter(([, code]) => !CHAR_BY_CODE.has(code))
    .map(([letters, code]) => [code, letters]),
);
