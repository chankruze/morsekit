import { TextLink } from "@/components/text-link";
import { PolicySection } from "@/screens/privacy/components/policy-section";
import { GOOGLE_PRIVACY_URL } from "@/screens/privacy/constants/links";

export const GooglePlaySection = () => (
  <PolicySection title="Google Play features">
    <p>
      On Android, MorseKit uses two features of the Google Play Store app:
      in-app updates (to tell you a new version is available) and in-app reviews
      (to let you rate MorseKit without leaving it). Both are provided by Google
      Play, which handles them under the{" "}
      <TextLink href={GOOGLE_PRIVACY_URL}>Google Privacy Policy</TextLink>.
      MorseKit only learns whether an update is available; it never learns
      whether you left a review or what it said.
    </p>
    <p>
      If you've allowed your device to share usage and diagnostics data with
      Google, Google Play may give the developer anonymous, aggregated
      statistics such as crash rates. These can't identify you.
    </p>
  </PolicySection>
);
