import { describe, expect, it } from "vitest";
import { buildSignalMarks } from "@/screens/home/utils/build-signal-marks";

describe("buildSignalMarks", () => {
  it("gives every element of every letter its own tone, in order", () => {
    const { letters, loopSeconds } = buildSignalMarks("SOS", 20, 1);
    expect(
      letters.map((l) => l.map((m) => (m.isDot ? "." : "-")).join("")),
    ).toEqual(["...", "---", "..."]);
    const starts = letters.flat().map((m) => m.tone.start);
    expect(starts).toEqual([...starts].sort((a, b) => a - b));
    expect(new Set(letters.flat().map((m) => m.id)).size).toBe(9);
    expect(loopSeconds).toBeGreaterThan(1);
  });
});
