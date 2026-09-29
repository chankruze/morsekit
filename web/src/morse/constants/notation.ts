export const DOT = ".";
export const DASH = "-";
export const LETTER_SEPARATOR = " ";
export const WORD_SEPARATOR = " / ";
export const REPLACEMENT_CHAR = "�";

export const DISPLAY_DOT = "•";
export const DISPLAY_DASH = "−";

export const ALTERNATIVE_DOTS: ReadonlySet<string> = new Set([
  "·",
  "•",
  "∙",
  "⋅",
]);
export const ALTERNATIVE_DASHES: ReadonlySet<string> = new Set([
  "−",
  "–",
  "—",
  "‒",
  "_",
]);
export const SINGLE_QUOTES: ReadonlySet<string> = new Set(["‘", "’", "‚", "′"]);
export const DOUBLE_QUOTES: ReadonlySet<string> = new Set(["“", "”", "„", "″"]);
export const MORSE_WORD_BREAKS: ReadonlySet<string> = new Set([
  "/",
  "|",
  "\n",
  "\r",
]);

/** Two or more spaces between Morse letters start a new word. */
export const WORD_BREAK_SPACES = 2;
