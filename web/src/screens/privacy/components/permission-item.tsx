import { memo } from "react";
import type { PolicyPermission } from "@/screens/privacy/types/privacy-policy";

type PermissionItemProps = {
  permission: PolicyPermission;
};

export const PermissionItem = memo(({ permission }: PermissionItemProps) => (
  <li>
    <strong className="text-cream">{permission.label}</strong>{" "}
    <code className="text-sm text-cream/60">{permission.name}</code>:{" "}
    {permission.why}
  </li>
));
