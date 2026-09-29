import { TextLink } from "@/components/text-link";
import { PolicySection } from "@/screens/privacy/components/policy-section";
import { CONTACT_EMAIL } from "@/screens/privacy/constants/links";

export const ContactSection = () => (
  <PolicySection title="Contact">
    <p>
      Questions about privacy? Email{" "}
      <TextLink href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</TextLink>.
    </p>
  </PolicySection>
);
