/**
 * International Morse, the same table as the app's `MorseAlphabet.International`
 * (shared/src/commonMain/kotlin/in/geekofia/morsekit/core/morse/MorseAlphabet.kt).
 * `alphabet.test.ts` reads the Kotlin file and fails if the two ever differ.
 */
export const ALPHABET: Readonly<Record<string, string>> = {
  A: ".-", B: "-...", C: "-.-.", D: "-..", E: ".",
  F: "..-.", G: "--.", H: "....", I: "..", J: ".---",
  K: "-.-", L: ".-..", M: "--", N: "-.", O: "---",
  P: ".--.", Q: "--.-", R: ".-.", S: "...", T: "-",
  U: "..-", V: "...-", W: ".--", X: "-..-", Y: "-.--",
  Z: "--..",

  0: "-----", 1: ".----", 2: "..---", 3: "...--", 4: "....-",
  5: ".....", 6: "-....", 7: "--...", 8: "---..", 9: "----.",

  ".": ".-.-.-", ",": "--..--", "?": "..--..", "'": ".----.",
  "/": "-..-.", "(": "-.--.", ")": "-.--.-", ":": "---...",
  "=": "-...-", "+": ".-.-.", "-": "-....-", '"': ".-..-.",
  "@": ".--.-.",

  // Non-ITU, but in common use.
  "!": "-.-.--", "&": ".-...", ";": "-.-.-.", _: "..--.-", $: "...-..-",
};

export const CHAR_BY_CODE: ReadonlyMap<string, string> = new Map(
  Object.entries(ALPHABET).map(([char, code]) => [code, char]),
);
