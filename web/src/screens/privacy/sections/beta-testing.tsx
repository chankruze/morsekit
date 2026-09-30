import { TextLink } from "@/components/text-link";
import {
  PLAY_TESTING_URL,
  TESTING_GROUP_EMAIL,
  TESTING_GROUP_URL,
} from "@/constants/links";
import { PolicySection } from "@/screens/privacy/components/policy-section";
import { GOOGLE_PRIVACY_URL } from "@/screens/privacy/constants/links";

export const BetaTestingSection = () => (
  <PolicySection title="Beta testing">
    <p>
      Testing early versions is optional. To take part, you join the{" "}
      <TextLink href={TESTING_GROUP_URL}>{TESTING_GROUP_EMAIL}</TextLink> Google
      Group and opt in on Google Play. The website doesn't collect your email
      address; you join on Google's own pages.
    </p>
    <p>
      As the group's owner, the developer can see members' names and email
      addresses. They're used only to give you access to test versions on Google
      Play, and are never shared or used for anything else. Google Groups and
      Google Play handle your membership under the{" "}
      <TextLink href={GOOGLE_PRIVACY_URL}>Google Privacy Policy</TextLink>.
    </p>
    <p>
      You can stop at any time: leave the test on the{" "}
      <TextLink href={PLAY_TESTING_URL}>Google Play testing page</TextLink> and
      leave the group, and your address is no longer on the list.
    </p>
  </PolicySection>
);
