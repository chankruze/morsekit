import { useCallback, useEffect, useRef, useState } from "react";
import { COPIED_FEEDBACK_MS } from "@/screens/home/constants/translator";

export const useClipboardCopy = () => {
  const [copied, setCopied] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined);

  useEffect(() => () => clearTimeout(timer.current), []);

  const copy = useCallback(async (text: string) => {
    await navigator.clipboard.writeText(text);
    setCopied(true);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setCopied(false), COPIED_FEEDBACK_MS);
  }, []);

  return { copied, copy };
};
