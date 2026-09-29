import type { ReactNode } from "react";
import { toDisplayGlyphs } from "@/morse/utils/to-display-glyphs";
import {
  DIRECTION_LABELS,
  EMPTY_OUTPUT,
  SKIPPED_PREFIXES,
} from "@/screens/home/constants/translator";
import type { TranslationDirection } from "@/screens/home/types/home";
import { oppositeDirection } from "@/screens/home/utils/opposite-direction";

type TranslatorOutputProps = {
  direction: TranslationDirection;
  output: string;
  skipped: string[];
  actions: ReactNode;
};

export const TranslatorOutput = ({
  direction,
  output,
  skipped,
  actions,
}: TranslatorOutputProps) => {
  const showsMorse = direction === "text";
  const shown = showsMorse ? toDisplayGlyphs(output) : output;

  return (
    <div className="mt-3 rounded-2xl bg-brand/30 p-4" aria-live="polite">
      <p className="text-xs font-medium tracking-wider text-cyan uppercase">
        {DIRECTION_LABELS[oppositeDirection(direction)]}
      </p>
      <p
        className={`mt-1 min-h-14 break-words ${
          showsMorse ? "text-2xl tracking-widest" : "text-xl"
        } ${output ? "" : "text-cream/40"}`}
      >
        {output ? shown : EMPTY_OUTPUT}
      </p>
      {skipped.length > 0 && (
        <p className="mt-2 text-sm text-sun">
          {SKIPPED_PREFIXES[direction]}
          {skipped.join(" ")}
        </p>
      )}
      {actions}
    </div>
  );
};
