import { SectionHeading } from "@/components/section-heading";
import { SECTION_IDS } from "@/constants/sections";
import { TranslatorCard } from "@/screens/home/components/translator-card";

export const TryItSection = () => (
  <section
    id={SECTION_IDS.tryIt}
    className="mx-auto max-w-3xl scroll-mt-20 px-5 py-16"
  >
    <SectionHeading className="text-center">Try it</SectionHeading>
    <p className="mt-3 mb-8 text-center text-cream/70">
      The same alphabet and timing as the app, right in your browser.
    </p>
    <TranslatorCard />
  </section>
);
