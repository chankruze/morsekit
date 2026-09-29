import { describe, expect, it } from "vitest";
import { encodeText } from "@/morse/utils/encode";

describe("encodeText", () => {
  it("encodes letters, digits and punctuation", () => {
    expect(encodeText("SOS").morse).toBe("... --- ...");
    expect(encodeText("Hi 5!").morse).toBe(".... .. / ..... -.-.--");
  });

  it("is case-insensitive and normalizes curly quotes", () => {
    expect(encodeText("sos").morse).toBe(encodeText("SOS").morse);
    expect(encodeText("‘x’ “y”").morse).toBe(encodeText(`'x' "y"`).morse);
  });

  it("collapses any whitespace into one word gap", () => {
    expect(encodeText("  A \n\t  B  ").morse).toBe(".- / -...");
  });

  it("skips and reports unsupported characters, once each, emoji as one", () => {
    const result = encodeText("A#B# 👍");
    expect(result.morse).toBe(".- -...");
    expect(result.unsupported).toEqual(["#", "👍"]);
  });

  it("gives empty output for empty input", () => {
    expect(encodeText("")).toEqual({ morse: "", unsupported: [] });
  });
});
