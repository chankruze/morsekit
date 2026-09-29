import { matchRoutes } from "react-router";
import { describe, expect, it } from "vitest";
import { ROUTES } from "@/constants/routes";
import { APP_ROUTES } from "@/providers/app-routes";

const PAGES_BASENAME = "/morsekit/";

const matchedPath = (url: string) =>
  matchRoutes(APP_ROUTES, url, PAGES_BASENAME)?.at(-1)?.route;

describe("routes on GitHub Pages", () => {
  it("serves the privacy policy at the Play Console URL, with or without the slash", () => {
    expect(matchedPath("/morsekit/privacy/")?.path).toBe(ROUTES.privacy);
    expect(matchedPath("/morsekit/privacy")?.path).toBe(ROUTES.privacy);
  });

  it("serves home at the site root", () => {
    expect(matchedPath("/morsekit/")?.index).toBe(true);
  });

  it("sends unknown paths to the catch-all", () => {
    expect(matchedPath("/morsekit/nope")?.path).toBe("*");
  });
});
