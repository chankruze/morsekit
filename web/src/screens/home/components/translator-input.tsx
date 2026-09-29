import {
  DIRECTION_LABELS,
  INPUT_ROWS,
  PLACEHOLDERS,
} from "@/screens/home/constants/translator";
import type { TranslationDirection } from "@/screens/home/types/home";

type TranslatorInputProps = {
  direction: TranslationDirection;
  value: string;
  onChange: (value: string) => void;
};

export const TranslatorInput = ({
  direction,
  value,
  onChange,
}: TranslatorInputProps) => (
  <label className="block">
    <span className="sr-only">{DIRECTION_LABELS[direction]} to translate</span>
    <textarea
      value={value}
      onChange={(event) => onChange(event.target.value)}
      placeholder={PLACEHOLDERS[direction]}
      rows={INPUT_ROWS}
      spellCheck={direction === "text"}
      className="w-full resize-none rounded-2xl bg-white/5 p-4 text-lg placeholder:text-cream/40 focus:bg-white/10 focus:outline-none"
    />
  </label>
);
