import { memo } from "react";
import type { SignalMark as SignalMarkData } from "@/screens/home/types/home";

type SignalMarkProps = {
  mark: SignalMarkData;
  lit: boolean;
};

export const SignalMark = memo(({ mark, lit }: SignalMarkProps) => (
  <span
    className={`h-3 rounded-full transition-[opacity,box-shadow] duration-75 ${
      mark.isDot ? "w-3" : "w-9"
    } ${mark.colourClass} ${lit ? "opacity-100 shadow-[0_0_14px]" : "opacity-20"}`}
  />
));
