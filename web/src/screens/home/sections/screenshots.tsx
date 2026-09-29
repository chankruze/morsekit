import { ScreenshotFrame } from "@/screens/home/components/screenshot-frame";
import { SCREENSHOTS } from "@/screens/home/constants/screenshots";

export const ScreenshotsSection = () => (
  <section aria-label="Screenshots" className="mx-auto max-w-6xl px-5 py-12">
    <ul className="-mx-5 flex snap-x snap-mandatory gap-5 overflow-x-auto px-5 pb-4 lg:mx-0 lg:grid lg:grid-cols-4 lg:overflow-visible lg:px-0">
      {SCREENSHOTS.map((screenshot, index) => (
        <ScreenshotFrame
          key={screenshot.src}
          screenshot={screenshot}
          staggered={index % 2 === 1}
        />
      ))}
    </ul>
  </section>
);
