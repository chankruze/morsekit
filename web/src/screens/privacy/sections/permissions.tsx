import { PermissionItem } from "@/screens/privacy/components/permission-item";
import { PolicySection } from "@/screens/privacy/components/policy-section";
import { PERMISSIONS } from "@/screens/privacy/constants/policy";

export const PermissionsSection = () => (
  <PolicySection title="Permissions">
    <ul className="space-y-2">
      {PERMISSIONS.map((permission) => (
        <PermissionItem key={permission.name} permission={permission} />
      ))}
    </ul>
    <p>MorseKit uses no other permissions.</p>
  </PolicySection>
);
