export const ROUTES = {
  home: "/",
  privacy: "/privacy",
} as const;

/** Router paths are relative to this: "/" locally, "/morsekit/" on GitHub Pages. */
export const ROUTER_BASENAME = import.meta.env.BASE_URL;
