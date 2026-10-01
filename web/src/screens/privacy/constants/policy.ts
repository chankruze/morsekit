import type {
  PolicyPermission,
  StoredDataKind,
} from "@/screens/privacy/types/privacy-policy";

// policy.test.ts checks these against the app's source, so the policy can't silently fall
// behind: a new permission or a new KEY_* prefix fails the web tests until it's described here.

/** Bump whenever the policy's meaning changes. */
export const EFFECTIVE_DATE = "1 October 2026";

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
    what: "Your settings: theme, Morse speeds (for playing and for tapping), how you tap (timing or buttons), tone pitch, and whether you've seen the flashlight warning.",
  },
  {
    prefix: "history",
    what: "History, if Save history is on: translations you copied, shared or sent (what you typed and what it became), newest first, and which ones you starred.",
  },
  {
    prefix: "review",
    what: "When to suggest rating the app: how many times you've used it successfully, and when it was first used and last asked.",
  },
  {
    prefix: "trainer",
    what: "Your Morse practice in Learn: which characters you've unlocked, how often you got each one right or wrong, your recent answers, whether the Morse is shown, and your practice mode (listening or keying) and session length.",
  },
  {
    prefix: "update",
    what: "When the app last checked for an update, and a version you chose to skip for a while.",
  },
];
