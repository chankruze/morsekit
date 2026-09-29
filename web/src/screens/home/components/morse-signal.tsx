import { useSignalClock } from "@/screens/home/hooks/use-signal-clock";
import { SignalMark } from "@/screens/home/components/signal-mark";
import {
  SIGNAL_PAUSE_SECONDS,
  SIGNAL_WORD,
  SIGNAL_WPM,
} from "@/screens/home/constants/morse-signal";
import { buildSignalMarks } from "@/screens/home/utils/build-signal-marks";
import { isMarkLit } from "@/screens/home/utils/is-mark-lit";

const { letters, loopSeconds } = buildSignalMarks(
  SIGNAL_WORD,
  SIGNAL_WPM,
  SIGNAL_PAUSE_SECONDS,
);

export const MorseSignal = () => {
  const now = useSignalClock(loopSeconds);

  return (
    <div
      className="flex flex-wrap items-center justify-center gap-x-5 gap-y-3"
      aria-hidden="true"
    >
      {letters.map((marks) => (
        <div key={marks[0].id} className="flex items-center gap-1.5">
          {marks.map((mark) => (
            <SignalMark key={mark.id} mark={mark} lit={isMarkLit(mark, now)} />
          ))}
        </div>
      ))}
    </div>
  );
};
