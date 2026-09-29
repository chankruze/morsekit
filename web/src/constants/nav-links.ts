import { SECTION_IDS } from "@/constants/sections";
import type { NavLink } from "@/types/site";

export const NAV_LINKS: NavLink[] = [
  { label: "Try it", section: SECTION_IDS.tryIt },
  { label: "Features", section: SECTION_IDS.features },
  { label: "Privacy", section: SECTION_IDS.privacy },
];
