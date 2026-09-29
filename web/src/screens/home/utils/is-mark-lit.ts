import type { SignalMark } from "@/screens/home/types/home";

/** Everything is lit when there's no clock (reduced motion). */
export const isMarkLit = (mark: SignalMark, now: number | null): boolean =>
  now === null ||
  (now >= mark.tone.start && now < mark.tone.start + mark.tone.duration);
