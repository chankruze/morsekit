import { describe, expect, it } from "vitest";
import { scheduleTones, unitSeconds } from "@/morse/utils/schedule-tones";

const units = (morse: string, wpm = 20) => {
  const unit = unitSeconds(wpm);
  const { tones, duration } = scheduleTones(morse, wpm);
  const round = (s: number) => Math.round((s / unit) * 1e6) / 1e6;
  return {
    tones: tones.map((t) => [round(t.start), round(t.duration)]),
    duration: round(duration),
  };
};

describe("scheduleTones", () => {
  it("uses the PARIS standard: 20 WPM is a 60 ms unit", () => {
    expect(unitSeconds(20)).toBeCloseTo(0.06);
  });

  it("times dots, dashes and the gap inside a letter", () => {
    // A = dot (0-1), gap 1, dash (2-5)
    expect(units(".-")).toEqual({
      tones: [
        [0, 1],
        [2, 3],
      ],
      duration: 5,
    });
  });

  it("puts 3 units between letters and 7 between words", () => {
    // E E / E
    expect(units(". . / .")).toEqual({
      tones: [
        [0, 1],
        [4, 1],
        [12, 1],
      ],
      duration: 13,
    });
  });

  it("PARIS plus its trailing word gap is 50 units", () => {
    expect(units(".--. .- .-. .. ...").duration + 7).toBe(50);
  });

  it("skips malformed tokens but keeps unknown well-formed codes", () => {
    expect(units("..x ........").tones).toHaveLength(8);
    expect(units("x / ?").tones).toEqual([]);
  });

  it("is empty for empty input", () => {
    expect(scheduleTones("")).toEqual({ tones: [], duration: 0 });
  });
});
