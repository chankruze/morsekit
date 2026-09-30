import { Link } from "react-router";
import { PLAY_PUBLISHED, PLAY_URL } from "@/constants/links";
import { ROUTES } from "@/constants/routes";
import { SECTION_IDS } from "@/constants/sections";
import { PlayStoreLogo } from "@/screens/home/components/play-store-logo";

const BUTTON_CLASS =
  "flex items-center gap-3 rounded-2xl border border-white/15 bg-black/40 px-5 py-2.5 transition hover:bg-black/60";

type PlayStoreLabelProps = {
  caption: string;
};

const PlayStoreLabel = ({ caption }: PlayStoreLabelProps) => (
  <>
    <PlayStoreLogo />
    <span className="text-left leading-tight">
      <span className="block text-xs text-cream/70">{caption}</span>
      <span className="block text-lg font-semibold">Google Play</span>
    </span>
  </>
);

/** Until the listing is public, the button leads to the closed-testing steps instead. */
export const PlayStoreButton = () =>
  PLAY_PUBLISHED ? (
    <a href={PLAY_URL} className={BUTTON_CLASS}>
      <PlayStoreLabel caption="Get it on" />
    </a>
  ) : (
    <Link
      to={{ pathname: ROUTES.home, hash: SECTION_IDS.beta }}
      className={BUTTON_CLASS}
    >
      <PlayStoreLabel caption="Join the beta on" />
    </Link>
  );
