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
      What you type is never sent anywhere. History is on by default: a
      translation is saved when you copy, share or send it (never while you're
      typing), so you can find it again, and starring one (☆) saves it as a
      favourite even when automatic saving is off. You can turn Save history
      off, delete entries, or clear it in Settings or History. None of this data
      identifies you, and it's deleted when you clear MorseKit's storage or
      uninstall the app.
    </p>
    <p>
      If you've turned on your device's backup (for example, Android backup to
      your Google account), your device may include your settings and practice
      progress in that backup so they come back on a new phone. History is never
      included: it's kept out of backups and device transfers, so your messages
      stay on this device. You control backup in your device settings; the
      backup belongs to your account and the developer can't access it.
    </p>
  </PolicySection>
);
