import { Link } from "react-router";
import { SectionHeading } from "@/components/section-heading";
import { ROUTES } from "@/constants/routes";
import { SECTION_IDS } from "@/constants/sections";
import { PrivacyHighlightItem } from "@/screens/home/components/privacy-highlight-item";
import { PRIVACY_HIGHLIGHTS } from "@/screens/home/constants/privacy-highlights";

export const PrivacySection = () => (
  <section
    id={SECTION_IDS.privacy}
    className="mx-auto max-w-6xl scroll-mt-20 px-5 py-16"
  >
    <div className="rounded-[2rem] bg-gradient-to-br from-brand to-navy p-8 sm:p-12">
      <SectionHeading className="max-w-xl">
        Works offline. Stays private.
      </SectionHeading>
      <ul className="mt-8 grid gap-6 sm:grid-cols-3">
        {PRIVACY_HIGHLIGHTS.map((highlight) => (
          <PrivacyHighlightItem key={highlight.title} highlight={highlight} />
        ))}
      </ul>
      <Link
        to={ROUTES.privacy}
        className="mt-8 inline-block font-medium text-cyan hover:underline"
      >
        Read the privacy policy →
      </Link>
    </div>
  </section>
);
