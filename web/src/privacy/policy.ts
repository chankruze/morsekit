/**
 * Facts the privacy policy states about the app. `policy.test.ts` checks them against the app's
 * source, so the policy can't silently fall behind: a new permission in AndroidManifest.xml or
 * a new kind of stored data (a new `KEY_*` prefix) fails the web tests until it's described here.
 */

/** Shown on the page; bump it whenever the policy's meaning changes. */
export const EFFECTIVE_DATE = "29 September 2026";

export const PERMISSIONS: { name: string; label: string; why: string }[] = [
  {
    name: "android.permission.VIBRATE",
    label: "Vibration",
    why: "To send Morse code as vibration when you choose Vibrate. It's granted at install and never asks.",
  },
];

/** What MorseKit keeps on the device, by the key prefix it's stored under. */
export const STORED_DATA: { prefix: string; what: string }[] = [
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
