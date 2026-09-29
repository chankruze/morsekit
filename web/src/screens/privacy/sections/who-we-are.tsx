import { TextLink } from "@/components/text-link";
import { ORGANIZATION_URL } from "@/constants/links";
import { PolicySection } from "@/screens/privacy/components/policy-section";

export const WhoWeAreSection = () => (
  <PolicySection title="Who we are">
    <p>
      MorseKit is an app for translating and sending Morse code, developed by
      chankruze at <TextLink href={ORGANIZATION_URL}>geekofia</TextLink>. This
      policy covers the MorseKit app and this website.
    </p>
  </PolicySection>
);
