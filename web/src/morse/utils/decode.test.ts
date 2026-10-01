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
    // Nine dots: no character and no prosign (eight dots is the prosign HH).
    const result = decodeMorse("... ......... ..x");
    expect(result.text).toBe(`S${REPLACEMENT_CHAR}${REPLACEMENT_CHAR}`);
    expect(result.unreadable).toEqual([".........", "..x"]);
  });

  it("writes prosign codes in angle brackets, like the app", () => {
    const result = decodeMorse("... ...---... / ........");
    expect(result.text).toBe("S<SOS> <HH>");
    expect(result.unreadable).toEqual([]);
  });

  it("keeps punctuation for the prosigns that share its code", () => {
    expect(decodeMorse(".-.-. -...- -.--. .-...").text).toBe("+=(&");
  });

  it("round-trips prosigns", () => {
    expect(decodeMorse(encodeText("SEND <SOS> NOW").morse).text).toBe(
      "SEND <SOS> NOW",
    );
  });

  it("round-trips everything the alphabet supports", () => {
    const text = "THE QUICK BROWN FOX 0123456789 .,?'/()=+-\"@!&;_$:";
    expect(decodeMorse(encodeText(text).morse).text).toBe(text);
  });
});
