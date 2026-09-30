import {
  PLAY_TESTING_URL,
  PLAY_URL,
  TESTING_GROUP_URL,
} from "@/constants/links";
import type { BetaStep } from "@/screens/home/types/home";

export const BETA_STEPS: BetaStep[] = [
  {
    title: "Join the testers group",
    text: "Sign in with the Google account on your Android phone and tap Join group.",
    links: [{ label: "Open the group", href: TESTING_GROUP_URL }],
  },
  {
    title: "Opt in on Google Play",
    text: "Accept the invitation to test MorseKit. It can take a few minutes after joining the group.",
    links: [{ label: "Become a tester", href: PLAY_TESTING_URL }],
  },
  {
    title: "Install MorseKit",
    text: "Get the test version from Google Play. Updates arrive like any other app.",
    links: [{ label: "Open on Google Play", href: PLAY_URL }],
  },
];
