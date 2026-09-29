import type { IconName } from "@/components/icon";
import type { Tone } from "@/morse/types/morse";

export type TranslationDirection = "text" | "morse";

export type Feature = {
  icon: IconName;
  title: string;
  text: string;
};

export type Screenshot = {
  src: string;
  alt: string;
};

export type PrivacyHighlight = {
  title: string;
  text: string;
};

export type SignalMark = {
  id: number;
  isDot: boolean;
  tone: Tone;
  colourClass: string;
};
