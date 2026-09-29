import { memo } from "react";
import { Icon } from "@/components/icon";
import type { Feature } from "@/screens/home/types/home";

type FeatureCardProps = {
  feature: Feature;
};

export const FeatureCard = memo(({ feature }: FeatureCardProps) => (
  <li className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
    <span className="inline-flex rounded-2xl bg-brand/40 p-3 text-cyan">
      <Icon name={feature.icon} className="size-6" />
    </span>
    <h3 className="mt-4 text-xl font-semibold">{feature.title}</h3>
    <p className="mt-2 text-cream/70">{feature.text}</p>
  </li>
));
