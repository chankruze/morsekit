import referenceLight from "@/assets/screenshots/reference-light.webp";
import settingsDark from "@/assets/screenshots/settings-dark.webp";
import translatorLight from "@/assets/screenshots/translator-light.webp";
import transmitDark from "@/assets/screenshots/translator-transmit-dark.webp";
import type { Screenshot } from "@/screens/home/types/home";

/** Intrinsic size of the WebP files, so the layout doesn't jump while they load. */
export const SCREENSHOT_WIDTH = 540;
export const SCREENSHOT_HEIGHT = 1157;

export const SCREENSHOTS: Screenshot[] = [
  {
    src: transmitDark,
    alt: "Translator in dark theme, with the Transmit menu open: speed, Sound, Flash and Vibrate",
  },
  {
    src: translatorLight,
    alt: "Translator in light theme, turning the letter d into dash dot dot",
  },
  {
    src: referenceLight,
    alt: "Reference chart of letters with their Morse codes",
  },
  {
    src: settingsDark,
    alt: "Settings in dark theme: theme, playback speed and tone",
  },
];
