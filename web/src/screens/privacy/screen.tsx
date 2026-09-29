import { PRIVACY_TITLE } from "@/constants/document-titles";
import { PrivacyLayout } from "@/screens/privacy/layout";
import { ChangesSection } from "@/screens/privacy/sections/changes";
import { ChildrenSection } from "@/screens/privacy/sections/children";
import { ContactSection } from "@/screens/privacy/sections/contact";
import { GooglePlaySection } from "@/screens/privacy/sections/google-play";
import { NotCollectedSection } from "@/screens/privacy/sections/not-collected";
import { OnDeviceSection } from "@/screens/privacy/sections/on-device";
import { PermissionsSection } from "@/screens/privacy/sections/permissions";
import { PolicyHeaderSection } from "@/screens/privacy/sections/policy-header";
import { SharingSection } from "@/screens/privacy/sections/sharing";
import { WebsiteSection } from "@/screens/privacy/sections/website";
import { WhoWeAreSection } from "@/screens/privacy/sections/who-we-are";

export const PrivacyScreen = () => (
  <PrivacyLayout>
    <title>{PRIVACY_TITLE}</title>
    <PolicyHeaderSection />
    <WhoWeAreSection />
    <NotCollectedSection />
    <OnDeviceSection />
    <PermissionsSection />
    <SharingSection />
    <GooglePlaySection />
    <ChildrenSection />
    <WebsiteSection />
    <ChangesSection />
    <ContactSection />
  </PrivacyLayout>
);
