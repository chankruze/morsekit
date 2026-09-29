import { TextLink } from "@/components/text-link";
import { PolicySection } from "@/screens/privacy/components/policy-section";
import { GITHUB_PRIVACY_URL } from "@/screens/privacy/constants/links";

export const WebsiteSection = () => (
  <PolicySection title="This website">
    <p>
      This website uses no cookies, no analytics and no third-party scripts or
      fonts. It's hosted on GitHub Pages, and GitHub may log technical data such
      as IP addresses to keep its service secure, under the{" "}
      <TextLink href={GITHUB_PRIVACY_URL}>
        GitHub General Privacy Statement
      </TextLink>
      .
    </p>
  </PolicySection>
);
