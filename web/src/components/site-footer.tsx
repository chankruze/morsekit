import logo from "@/assets/logo.png";
import { DEVELOPER_URL, ORGANIZATION_URL } from "@/constants/links";

const FOOTER_LINK_CLASS = "text-cream/85 hover:text-cream";

export const SiteFooter = () => (
  <footer className="border-t border-white/5">
    <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-5 py-8 text-sm text-cream/60 sm:flex-row">
      <p className="flex items-center gap-2">
        <img src={logo} alt="" className="size-5 rounded" />
        MorseKit {__APP_VERSION__}
      </p>
      <p>
        Made by{" "}
        <a href={DEVELOPER_URL} className={FOOTER_LINK_CLASS}>
          chankruze
        </a>{" "}
        at{" "}
        <a href={ORGANIZATION_URL} className={FOOTER_LINK_CLASS}>
          geekofia
        </a>
      </p>
    </div>
  </footer>
);
