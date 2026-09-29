import { Icon } from "@/components/icon";
import { DIRECTION_LABELS } from "@/screens/home/constants/translator";
import { oppositeDirection } from "@/screens/home/utils/opposite-direction";
import type { TranslationDirection } from "@/screens/home/types/home";

type DirectionBarProps = {
  direction: TranslationDirection;
  onSwap: () => void;
};

const CHIP_CLASS = "rounded-full bg-white/10 px-4 py-1.5 text-sm font-medium";

export const DirectionBar = ({ direction, onSwap }: DirectionBarProps) => {
  const from = DIRECTION_LABELS[direction];
  const to = DIRECTION_LABELS[oppositeDirection(direction)];

  return (
    <div className="mb-4 flex items-center justify-between gap-3">
      <span className={CHIP_CLASS}>{from}</span>
      <button
        type="button"
        onClick={onSwap}
        className="rounded-full p-2 text-cyan transition hover:bg-white/10"
        aria-label={`Swap: translate ${to} to ${from}`}
      >
        <Icon name="translate" className="size-6" />
      </button>
      <span className={CHIP_CLASS}>{to}</span>
    </div>
  );
};
