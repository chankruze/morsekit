import type {
  PolicyPermission,
  StoredDataKind,
} from "@/screens/privacy/types/privacy-policy";

// policy.test.ts checks these against the app's source, so the policy can't silently fall
// behind: a new permission or a new KEY_* prefix fails the web tests until it's described here.

/** Bump whenever the policy's meaning changes. */
export const EFFECTIVE_DATE = "29 September 2026";

export const PERMISSIONS: PolicyPermission[] = [
  {
    name: "android.permission.VIBRATE",
    label: "Vibration",
    why: "To send Morse code as vibration when you choose Vibrate. It's granted at install and never asks.",
  },
];

export const STORED_DATA: StoredDataKind[] = [
  {
    prefix: "settings",
    what: "Your settings: theme, Morse speed, tone pitch, and whether you've seen the flashlight warning.",
  },
  {
    prefix: "review",
    what: "When to suggest rating the app: how many times you've used it successfully, and when it was first used and last asked.",
  },
  {
    prefix: "update",
    what: "When the app last checked for an update, and a version you chose to skip for a while.",
  },
];
