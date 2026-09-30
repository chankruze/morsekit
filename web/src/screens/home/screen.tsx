import { HOME_TITLE } from "@/constants/document-titles";
import { BetaSection } from "@/screens/home/sections/beta";
import { FeaturesSection } from "@/screens/home/sections/features";
import { HeroSection } from "@/screens/home/sections/hero";
import { PrivacySection } from "@/screens/home/sections/privacy";
import { ScreenshotsSection } from "@/screens/home/sections/screenshots";
import { TryItSection } from "@/screens/home/sections/try-it";

export const HomeScreen = () => (
  <>
    <title>{HOME_TITLE}</title>
    <HeroSection />
    <ScreenshotsSection />
    <TryItSection />
    <FeaturesSection />
    <BetaSection />
    <PrivacySection />
  </>
);
