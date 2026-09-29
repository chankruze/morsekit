import type { TranslationDirection } from "@/screens/home/types/home";

export const DEFAULT_INPUT = "Hello world";
export const COPIED_FEEDBACK_MS = 1500;
export const INPUT_ROWS = 3;

export const DIRECTION_LABELS: Record<TranslationDirection, string> = {
  text: "Text",
  morse: "Morse",
};

export const PLACEHOLDERS: Record<TranslationDirection, string> = {
  text: "Type a message…",
  morse: "Type dots and dashes, e.g. ... --- ...",
};

export const SKIPPED_PREFIXES: Record<TranslationDirection, string> = {
  text: "No Morse for: ",
  morse: "Couldn't read: ",
};

export const EMPTY_OUTPUT = "Translation appears here";
