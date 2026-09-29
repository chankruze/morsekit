import { useEffect } from "react";
import { useLocation } from "react-router";

/** React Router doesn't scroll on navigation: go to the #section, or to the top of a new page. */
export const useScrollToHash = () => {
  const { pathname, hash, key } = useLocation();

  useEffect(() => {
    if (!hash) {
      window.scrollTo({ top: 0 });
      return;
    }
    document.getElementById(hash.slice(1))?.scrollIntoView();
  }, [pathname, hash, key]);
};
