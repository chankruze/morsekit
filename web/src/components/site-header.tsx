import { Link } from "react-router";
import logo from "@/assets/logo.png";
import { GitHubMark } from "@/components/github-mark";
import { GITHUB_URL } from "@/constants/links";
import { NAV_LINKS } from "@/constants/nav-links";
import { ROUTES } from "@/constants/routes";

const NAV_LINK_CLASS =
  "rounded-full px-3 py-1.5 text-sm text-cream/80 transition hover:bg-white/10 hover:text-cream";

export const SiteHeader = () => (
  <header className="sticky top-0 z-10 border-b border-white/5 bg-night/70 backdrop-blur">
    <nav
      className="mx-auto flex max-w-6xl items-center justify-between px-5 py-3"
      aria-label="Main"
    >
      <Link
        to={ROUTES.home}
        className="flex items-center gap-2.5 font-semibold"
      >
        <img src={logo} alt="" className="size-8 rounded-lg" />
        MorseKit
      </Link>
      <div className="flex items-center gap-1">
        {NAV_LINKS.map((link) => (
          <Link
            key={link.section}
            to={{ pathname: ROUTES.home, hash: link.section }}
            className={`${NAV_LINK_CLASS} hidden sm:block`}
          >
            {link.label}
          </Link>
        ))}
        <a
          href={GITHUB_URL}
          className={`${NAV_LINK_CLASS} flex items-center gap-2`}
        >
          <GitHubMark className="size-4" />
          GitHub
        </a>
      </div>
    </nav>
  </header>
);
