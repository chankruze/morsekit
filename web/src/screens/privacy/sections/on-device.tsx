import { PolicySection } from "@/screens/privacy/components/policy-section";
import { StoredDataItem } from "@/screens/privacy/components/stored-data-item";
import { STORED_DATA } from "@/screens/privacy/constants/policy";

export const OnDeviceSection = () => (
  <PolicySection title="What stays on your device">
    <p>
      To remember your preferences, MorseKit stores a few values on your device
      only:
    </p>
    <ul className="list-disc space-y-2 pl-6">
      {STORED_DATA.map((kind) => (
        <StoredDataItem key={kind.prefix} kind={kind} />
      ))}
    </ul>
    <p>
      What you type into the translator isn't kept as a history and is never
      sent anywhere. None of this data identifies you, and it's deleted when you
      clear MorseKit's storage or uninstall the app.
    </p>
    <p>
      If you've turned on your device's backup (for example, Android backup to
      your Google account), your device may include these settings in that
      backup so they come back on a new phone. You control this in your device
      settings; the backup belongs to your account and the developer can't
      access it.
    </p>
  </PolicySection>
);
