import { describe, expect, it } from "vitest";
import { splitMorseWords } from "@/morse/utils/split-morse-words";

describe("splitMorseWords", () => {
  it("splits letters on one space and words on two", () => {
    expect(splitMorseWords(".-  -... -.-.")).toEqual([
      [".-"],
      ["-...", "-.-."],
    ]);
  });
});
