import { PLAY_PUBLISHED, PLAY_URL } from "@/constants/links";
import { PlayStoreLogo } from "@/screens/home/components/play-store-logo";

const BUTTON_CLASS =
  "flex items-center gap-3 rounded-2xl border border-white/15 bg-black/40 px-5 py-2.5";

const PlayStoreLabel = () => (
  <>
    <PlayStoreLogo />
    <span className="text-left leading-tight">
      <span className="block text-xs text-cream/70">
        {PLAY_PUBLISHED ? "Get it on" : "Coming soon to"}
      </span>
      <span className="block text-lg font-semibold">Google Play</span>
    </span>
  </>
);

export const PlayStoreButton = () =>
  PLAY_PUBLISHED ? (
    <a
      href={PLAY_URL}
      className={`${BUTTON_CLASS} transition hover:bg-black/60`}
    >
      <PlayStoreLabel />
    </a>
  ) : (
    <div className={`${BUTTON_CLASS} opacity-80`}>
      <PlayStoreLabel />
    </div>
  );
