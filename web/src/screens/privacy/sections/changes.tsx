import { TextLink } from "@/components/text-link";
import { PolicySection } from "@/screens/privacy/components/policy-section";
import { POLICY_HISTORY_URL } from "@/screens/privacy/constants/links";

export const ChangesSection = () => (
  <PolicySection title="Changes to this policy">
    <p>
      If this policy changes, the new version will be posted on this page with a
      new effective date. Every past version is public in the{" "}
      <TextLink href={POLICY_HISTORY_URL}>project's history on GitHub</TextLink>
      .
    </p>
  </PolicySection>
);
