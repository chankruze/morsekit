import { Outlet, useMatch } from "react-router";
import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";
import { ROUTES } from "@/constants/routes";
import { useScrollToHash } from "@/hooks/use-scroll-to-hash";

export const SiteLayout = () => {
  const onHome = useMatch(ROUTES.home) !== null;
  useScrollToHash();

  return (
    <div className="relative overflow-x-clip">
      {onHome && <div aria-hidden="true" className="hero-glow" />}
      <SiteHeader />
      <main>
        <Outlet />
      </main>
      <SiteFooter />
    </div>
  );
};
