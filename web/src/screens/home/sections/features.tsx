import { SectionHeading } from "@/components/section-heading";
import { SECTION_IDS } from "@/constants/sections";
import { FeatureCard } from "@/screens/home/components/feature-card";
import { FEATURES } from "@/screens/home/constants/features";

export const FeaturesSection = () => (
  <section
    id={SECTION_IDS.features}
    className="mx-auto max-w-6xl scroll-mt-20 px-5 py-16"
  >
    <SectionHeading className="text-center">
      Everything for Morse
    </SectionHeading>
    <ul className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {FEATURES.map((feature) => (
        <FeatureCard key={feature.title} feature={feature} />
      ))}
    </ul>
  </section>
);
