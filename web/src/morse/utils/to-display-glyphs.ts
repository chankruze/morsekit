import {
  DASH,
  DISPLAY_DASH,
  DISPLAY_DOT,
  DOT,
} from "@/morse/constants/notation";

/** `.-` → `•−`, the glyphs the app shows on screen. */
export const toDisplayGlyphs = (morse: string): string =>
  morse.replaceAll(DOT, DISPLAY_DOT).replaceAll(DASH, DISPLAY_DASH);
