import { Link } from "react-router";
import { SectionHeading } from "@/components/section-heading";
import { TESTING_GROUP_EMAIL } from "@/constants/links";
import { ROUTES } from "@/constants/routes";
import { SECTION_IDS } from "@/constants/sections";
import { BetaStepCard } from "@/screens/home/components/beta-step-card";
import { BETA_STEPS } from "@/screens/home/constants/beta-steps";

export const BetaSection = () => (
  <section
    id={SECTION_IDS.beta}
    className="mx-auto max-w-6xl scroll-mt-20 px-5 py-16"
  >
    <SectionHeading className="text-center">Join the beta</SectionHeading>
    <p className="mx-auto mt-3 max-w-2xl text-center text-balance text-cream/70">
      MorseKit is in closed testing on Google Play. Try it before everyone else
      and help shape it: three steps, about a minute.
    </p>
    <ol className="mt-10 grid gap-4 md:grid-cols-3">
      {BETA_STEPS.map((step, index) => (
        <BetaStepCard key={step.title} step={step} number={index + 1} />
      ))}
    </ol>
    <p className="mt-6 text-center text-sm text-cream/60">
      Testers are the members of {TESTING_GROUP_EMAIL}. Use the same Google
      account on your phone, and leave any time.{" "}
      <Link to={ROUTES.privacy} className="text-cyan hover:underline">
        How your email is used
      </Link>
    </p>
  </section>
);
