import { useCallback } from "react";
import { DirectionBar } from "@/screens/home/components/direction-bar";
import { TranslatorActions } from "@/screens/home/components/translator-actions";
import { TranslatorInput } from "@/screens/home/components/translator-input";
import { TranslatorOutput } from "@/screens/home/components/translator-output";
import { useClipboardCopy } from "@/screens/home/hooks/use-clipboard-copy";
import { useMorsePlayback } from "@/screens/home/hooks/use-morse-playback";
import { useTranslator } from "@/screens/home/hooks/use-translator";

export const TranslatorCard = () => {
  const { direction, input, setInput, swap, output, morseToPlay, skipped } =
    useTranslator();
  const { playing, toggle, canPlay } = useMorsePlayback(
    morseToPlay,
    `${direction}:${input}`,
  );
  const { copied, copy } = useClipboardCopy();
  const copyOutput = useCallback(() => copy(output), [copy, output]);

  return (
    <div className="rounded-3xl border border-white/10 bg-navy/60 p-4 shadow-2xl shadow-black/40 backdrop-blur sm:p-6">
      <DirectionBar direction={direction} onSwap={swap} />
      <TranslatorInput
        direction={direction}
        value={input}
        onChange={setInput}
      />
      <TranslatorOutput
        direction={direction}
        output={output}
        skipped={skipped}
        actions={
          <TranslatorActions
            canCopy={output.length > 0}
            copied={copied}
            onCopy={copyOutput}
            canPlay={canPlay}
            playing={playing}
            onTogglePlay={toggle}
          />
        }
      />
    </div>
  );
};
