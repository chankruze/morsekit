import { describe, expect, it } from "vitest";
import { toDisplayGlyphs } from "@/morse/utils/to-display-glyphs";

describe("toDisplayGlyphs", () => {
  it("uses the app's on-screen glyphs", () => {
    expect(toDisplayGlyphs("... --- ...")).toBe("••• −−− •••");
  });
});
