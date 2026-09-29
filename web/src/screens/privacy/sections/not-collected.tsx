import { PolicySection } from "@/screens/privacy/components/policy-section";

export const NotCollectedSection = () => (
  <PolicySection title="What MorseKit doesn't collect">
    <p>
      MorseKit doesn't collect personal information of any kind. There are no
      accounts, no analytics or crash-reporting tools, no advertising, and no
      tracking. The app can't reach the internet: it doesn't request Android's
      internet permission, so it has no way to send anything anywhere.
    </p>
    <p>
      It doesn't access your location, contacts, photos, camera or microphone.
      Flashing the flashlight uses Android's torch control, which doesn't need
      camera access, and nothing is ever recorded.
    </p>
  </PolicySection>
);
