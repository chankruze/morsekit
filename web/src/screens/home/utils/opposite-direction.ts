import type { TranslationDirection } from "@/screens/home/types/home";

export const oppositeDirection = (
  direction: TranslationDirection,
): TranslationDirection => (direction === "text" ? "morse" : "text");
