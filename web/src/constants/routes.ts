export const ROUTES = {
  home: "/",
  privacy: "/privacy",
} as const;

/** Router paths are relative to this: "/" locally and on morsekit.geekofia.in. */
export const ROUTER_BASENAME = import.meta.env.BASE_URL;
