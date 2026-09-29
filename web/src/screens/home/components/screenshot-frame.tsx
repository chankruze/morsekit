import { memo } from "react";
import {
  SCREENSHOT_HEIGHT,
  SCREENSHOT_WIDTH,
} from "@/screens/home/constants/screenshots";
import type { Screenshot } from "@/screens/home/types/home";

type ScreenshotFrameProps = {
  screenshot: Screenshot;
  /** Every other screenshot sits lower on wide screens, for a staggered row. */
  staggered: boolean;
};

export const ScreenshotFrame = memo(
  ({ screenshot, staggered }: ScreenshotFrameProps) => (
    <li
      className={`w-60 shrink-0 snap-center lg:w-auto ${staggered ? "lg:translate-y-8" : ""}`}
    >
      <img
        src={screenshot.src}
        alt={screenshot.alt}
        width={SCREENSHOT_WIDTH}
        height={SCREENSHOT_HEIGHT}
        loading="lazy"
        className="w-full rounded-[2rem] border-4 border-white/10 shadow-2xl shadow-black/50"
      />
    </li>
  ),
);
