import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import {
  PROSIGN_BY_CODE,
  PROSIGN_CODE_BY_LETTERS,
  PROSIGN_LETTERS,
} from "@/morse/constants/prosigns";
import { repoPath } from "@/utils/repo-path.node";

const KOTLIN_PROSIGNS =
  "shared/src/commonMain/kotlin/in/geekofia/morsekit/core/morse/MorseProsigns.kt";

describe("prosigns", () => {
  it("are exactly the app's, in the same order", () => {
    const source = readFileSync(repoPath(KOTLIN_PROSIGNS), "utf8");
    const app = [...source.matchAll(/prosign\("([A-Z]+)",/g)].map((m) => m[1]);
    expect(app.length).toBe(10);
    expect(PROSIGN_LETTERS).toEqual(app);
  });

  it("have the right codes", () => {
    expect(PROSIGN_CODE_BY_LETTERS.get("SOS")).toBe("...---...");
    expect(PROSIGN_CODE_BY_LETTERS.get("HH")).toBe("........");
    expect(PROSIGN_CODE_BY_LETTERS.get("CL")).toBe("-.-..-..");
  });

  it("leave out the four that share a code with punctuation when decoding", () => {
    expect([...PROSIGN_BY_CODE.values()].sort()).toEqual([
      "CL",
      "CT",
      "HH",
      "SK",
      "SOS",
      "VE",
    ]);
  });
});
