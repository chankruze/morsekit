import logo from "../assets/logo.png";
import { DEVELOPER_URL, GITHUB_URL, ORGANIZATION_URL } from "../links";

/** The site's root, e.g. "/" locally or "/morsekit/" on GitHub Pages. */
export const HOME = import.meta.env.BASE_URL;
export const PRIVACY_POLICY = `${HOME}privacy/`;

function GitHubMark({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 16 16"
      fill="currentColor"
      aria-hidden="true"
      className={className}
    >
      <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z" />
    </svg>
  );
}

const navLink =
  "rounded-full px-3 py-1.5 text-sm text-cream/80 transition hover:bg-white/10 hover:text-cream";

/** [onHome]: links to sections are plain anchors on the home page, full links elsewhere. */
export function SiteHeader({ onHome = false }: { onHome?: boolean }) {
  const section = (id: string) => (onHome ? `#${id}` : `${HOME}#${id}`);
  return (
    <header className="sticky top-0 z-10 border-b border-white/5 bg-night/70 backdrop-blur">
      <nav
        className="mx-auto flex max-w-6xl items-center justify-between px-5 py-3"
        aria-label="Main"
      >
        <a
          href={onHome ? "#top" : HOME}
          className="flex items-center gap-2.5 font-semibold"
        >
          <img src={logo} alt="" className="size-8 rounded-lg" />
          MorseKit
        </a>
        <div className="flex items-center gap-1">
          <a href={section("try")} className={`${navLink} hidden sm:block`}>
            Try it
          </a>
          <a
            href={section("features")}
            className={`${navLink} hidden sm:block`}
          >
            Features
          </a>
          <a
            href={section("privacy")}
            className={`${navLink} hidden sm:block`}
          >
            Privacy
          </a>
          <a href={GITHUB_URL} className={`${navLink} flex items-center gap-2`}>
            <GitHubMark className="size-4" />
            GitHub
          </a>
        </div>
      </nav>
    </header>
  );
}

export function SiteFooter() {
  return (
    <footer className="border-t border-white/5">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-5 py-8 text-sm text-cream/60 sm:flex-row">
        <p className="flex items-center gap-2">
          <img src={logo} alt="" className="size-5 rounded" />
          MorseKit {__APP_VERSION__}
        </p>
        <p>
          <a href={PRIVACY_POLICY} className="text-cream/85 hover:text-cream">
            Privacy policy
          </a>
        </p>
        <p>
          Made by{" "}
          <a href={DEVELOPER_URL} className="text-cream/85 hover:text-cream">
            chankruze
          </a>{" "}
          at{" "}
          <a href={ORGANIZATION_URL} className="text-cream/85 hover:text-cream">
            geekofia
          </a>
        </p>
      </div>
    </footer>
  );
}
