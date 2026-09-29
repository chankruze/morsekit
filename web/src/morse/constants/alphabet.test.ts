import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { ALPHABET } from "@/morse/constants/alphabet";
import { repoPath } from "@/utils/repo-path.node";

const KOTLIN_ALPHABET =
  "shared/src/commonMain/kotlin/in/geekofia/morsekit/core/morse/MorseAlphabet.kt";
const APP_ALPHABET_SIZE = 54;

const kotlinAlphabet = (): Record<string, string> => {
  const source = readFileSync(repoPath(KOTLIN_ALPHABET), "utf8");
  const pairs = [...source.matchAll(/'(\\'|[^'])' to "([.-]+)"/g)];
  return Object.fromEntries(
    pairs.map(([, char, code]) => [char.replace("\\'", "'"), code]),
  );
};

describe("alphabet", () => {
  it("is exactly the app's alphabet", () => {
    const app = kotlinAlphabet();
    expect(Object.keys(app).length).toBe(APP_ALPHABET_SIZE);
    expect(ALPHABET).toEqual(app);
  });

  it("has no duplicate codes", () => {
    const codes = Object.values(ALPHABET);
    expect(new Set(codes).size).toBe(codes.length);
  });
});
