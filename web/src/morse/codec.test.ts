import { describe, expect, it } from "vitest";
import { REPLACEMENT_CHAR, decode, encode, morseWords, toDisplayGlyphs } from "./codec";

describe("encode", () => {
  it("encodes letters, digits and punctuation", () => {
    expect(encode("SOS").morse).toBe("... --- ...");
    expect(encode("Hi 5!").morse).toBe(".... .. / ..... -.-.--");
  });

  it("is case-insensitive and normalizes curly quotes", () => {
    expect(encode("sos").morse).toBe(encode("SOS").morse);
    expect(encode("‘x’ “y”").morse).toBe(encode(`'x' "y"`).morse);
  });

  it("collapses any whitespace into one word gap", () => {
    expect(encode("  A \n\t  B  ").morse).toBe(".- / -...");
  });

  it("skips and reports unsupported characters, once each, emoji as one", () => {
    const result = encode("A#B# 👍");
    expect(result.morse).toBe(".- -...");
    expect(result.unsupported).toEqual(["#", "👍"]);
  });

  it("gives empty output for empty input", () => {
    expect(encode("")).toEqual({ morse: "", unsupported: [] });
  });
});

describe("decode", () => {
  it("decodes canonical Morse", () => {
    expect(decode("... --- ... / .... ..").text).toBe("SOS HI");
  });

  it("accepts the other word breaks and dot/dash glyphs", () => {
    expect(decode("•−  −•••|.-\n-...").text).toBe("A B A B");
    expect(decode("._ _...").text).toBe("AB");
  });

  it("replaces and reports unknown or malformed letters", () => {
    const result = decode("... ........ ..x");
    expect(result.text).toBe(`S${REPLACEMENT_CHAR}${REPLACEMENT_CHAR}`);
    expect(result.unreadable).toEqual(["........", "..x"]);
  });

  it("round-trips everything the alphabet supports", () => {
    const text = "THE QUICK BROWN FOX 0123456789 .,?'/()=+-\"@!&;_$:";
    expect(decode(encode(text).morse).text).toBe(text);
  });
});

describe("morseWords", () => {
  it("splits letters on one space and words on two", () => {
    expect(morseWords(".-  -... -.-.")).toEqual([[".-"], ["-...", "-.-."]]);
  });
});

describe("toDisplayGlyphs", () => {
  it("uses the app's on-screen glyphs", () => {
    expect(toDisplayGlyphs("... --- ...")).toBe("••• −−− •••");
  });
});
