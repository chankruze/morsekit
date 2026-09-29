import { memo } from "react";
import type { PrivacyHighlight } from "@/screens/home/types/home";

type PrivacyHighlightItemProps = {
  highlight: PrivacyHighlight;
};

export const PrivacyHighlightItem = memo(
  ({ highlight }: PrivacyHighlightItemProps) => (
    <li>
      <h3 className="flex items-center gap-2 text-lg font-semibold">
        <span className="size-2.5 rounded-full bg-sun" aria-hidden="true" />
        {highlight.title}
      </h3>
      <p className="mt-2 text-cream/80">{highlight.text}</p>
    </li>
  ),
);
