import { memo } from "react";
import type { BetaStep } from "@/screens/home/types/home";

type BetaStepCardProps = {
  step: BetaStep;
  number: number;
};

export const BetaStepCard = memo(({ step, number }: BetaStepCardProps) => (
  <li className="flex flex-col rounded-3xl border border-white/10 bg-white/[0.03] p-6">
    <span
      className="flex size-9 items-center justify-center rounded-full bg-sun font-bold text-night"
      aria-hidden="true"
    >
      {number}
    </span>
    <h3 className="mt-4 text-xl font-semibold">{step.title}</h3>
    <p className="mt-2 flex-1 text-cream/70">{step.text}</p>
    <div className="mt-5 flex flex-wrap gap-2">
      {step.links.map((link) => (
        <a
          key={link.href}
          href={link.href}
          target="_blank"
          rel="noreferrer"
          className="rounded-full bg-cyan px-4 py-2 text-sm font-semibold text-night transition hover:bg-cyan/85"
        >
          {link.label} ↗
        </a>
      ))}
    </div>
  </li>
));
