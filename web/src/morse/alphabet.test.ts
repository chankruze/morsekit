import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { ALPHABET } from "./alphabet";

const KOTLIN_ALPHABET = fileURLToPath(
  new URL(
    "../../../shared/src/commonMain/kotlin/in/geekofia/morsekit/core/morse/MorseAlphabet.kt",
    import.meta.url,
  ),
);

/** Reads the `'A' to ".-"` pairs out of the app's Kotlin source. */
function kotlinAlphabet(): Record<string, string> {
  const source = readFileSync(KOTLIN_ALPHABET, "utf8");
  const pairs = [...source.matchAll(/'(\\'|[^'])' to "([.-]+)"/g)];
  return Object.fromEntries(pairs.map(([, char, code]) => [char.replace("\\'", "'"), code]));
}

describe("alphabet", () => {
  it("is exactly the app's alphabet", () => {
    const app = kotlinAlphabet();
    expect(Object.keys(app).length).toBe(54);
    expect(ALPHABET).toEqual(app);
  });

  it("has no duplicate codes", () => {
    const codes = Object.values(ALPHABET);
    expect(new Set(codes).size).toBe(codes.length);
  });
});
