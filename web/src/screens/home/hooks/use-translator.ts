import { useCallback, useMemo, useState } from "react";
import { decodeMorse } from "@/morse/utils/decode";
import { encodeText } from "@/morse/utils/encode";
import { DEFAULT_INPUT } from "@/screens/home/constants/translator";
import type { TranslationDirection } from "@/screens/home/types/home";
import { oppositeDirection } from "@/screens/home/utils/opposite-direction";

export type Translation = {
  output: string;
  /** What Play sounds: the encoded Morse, or the Morse as typed. */
  morseToPlay: string;
  skipped: string[];
};

const translate = (
  direction: TranslationDirection,
  input: string,
): Translation => {
  if (direction === "text") {
    const { morse, unsupported } = encodeText(input);
    return { output: morse, morseToPlay: morse, skipped: unsupported };
  }
  const { text, unreadable } = decodeMorse(input);
  return { output: text, morseToPlay: input, skipped: unreadable };
};

export const useTranslator = () => {
  const [direction, setDirection] = useState<TranslationDirection>("text");
  const [input, setInput] = useState(DEFAULT_INPUT);
  const translation = useMemo(
    () => translate(direction, input),
    [direction, input],
  );

  const swap = useCallback(() => {
    setInput(translation.output);
    setDirection(oppositeDirection);
  }, [translation.output]);

  return { direction, input, setInput, swap, ...translation };
};
