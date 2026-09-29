import { Icon } from "@/components/icon";

type TranslatorActionsProps = {
  canCopy: boolean;
  copied: boolean;
  onCopy: () => void;
  canPlay: boolean;
  playing: boolean;
  onTogglePlay: () => void;
};

export const TranslatorActions = ({
  canCopy,
  copied,
  onCopy,
  canPlay,
  playing,
  onTogglePlay,
}: TranslatorActionsProps) => (
  <div className="mt-3 flex justify-end gap-2">
    <button
      type="button"
      onClick={onCopy}
      disabled={!canCopy}
      className="flex items-center gap-2 rounded-full px-4 py-2 text-sm transition hover:bg-white/10 disabled:opacity-40"
    >
      <Icon name="copy" className="size-4" />
      {copied ? "Copied" : "Copy"}
    </button>
    <button
      type="button"
      onClick={onTogglePlay}
      disabled={!canPlay}
      className={`flex items-center gap-2 rounded-full px-5 py-2 text-sm font-semibold text-night transition disabled:opacity-40 ${
        playing ? "bg-ember" : "bg-cyan hover:bg-cyan/85"
      }`}
    >
      <Icon name={playing ? "stop" : "volume"} className="size-4" />
      {playing ? "Stop" : "Play sound"}
    </button>
  </div>
);
