export type PolicyPermission = {
  name: string;
  label: string;
  why: string;
};

export type StoredDataKind = {
  /** The `KEY_*` prefix the app stores it under, e.g. `settings`. */
  prefix: string;
  what: string;
};
