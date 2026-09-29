import { describe, expect, it } from "vitest";
import { REPLACEMENT_CHAR } from "@/morse/constants/notation";
import { decodeMorse } from "@/morse/utils/decode";
import { encodeText } from "@/morse/utils/encode";

describe("decodeMorse", () => {
  it("decodes canonical Morse", () => {
    expect(decodeMorse("... --- ... / .... ..").text).toBe("SOS HI");
  });

  it("accepts the other word breaks and dot/dash glyphs", () => {
    expect(decodeMorse("•−  −•••|.-\n-...").text).toBe("A B A B");
    expect(decodeMorse("._ _...").text).toBe("AB");
  });

  it("replaces and reports unknown or malformed letters", () => {
    const result = decodeMorse("... ........ ..x");
    expect(result.text).toBe(`S${REPLACEMENT_CHAR}${REPLACEMENT_CHAR}`);
    expect(result.unreadable).toEqual(["........", "..x"]);
  });

  it("round-trips everything the alphabet supports", () => {
    const text = "THE QUICK BROWN FOX 0123456789 .,?'/()=+-\"@!&;_$:";
    expect(decodeMorse(encodeText(text).morse).text).toBe(text);
  });
});
