import { memo } from "react";
import type { StoredDataKind } from "@/screens/privacy/types/privacy-policy";

type StoredDataItemProps = {
  kind: StoredDataKind;
};

export const StoredDataItem = memo(({ kind }: StoredDataItemProps) => (
  <li>{kind.what}</li>
));
