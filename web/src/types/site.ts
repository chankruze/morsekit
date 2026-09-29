import type { SECTION_IDS } from "@/constants/sections";

export type SectionId = (typeof SECTION_IDS)[keyof typeof SECTION_IDS];

export type NavLink = {
  label: string;
  section: SectionId;
};
