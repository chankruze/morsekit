import { matchRoutes } from "react-router";
import { describe, expect, it } from "vitest";
import { ROUTES } from "@/constants/routes";
import { APP_ROUTES } from "@/providers/app-routes";

// "/" on the custom domain (morsekit.geekofia.in); a sub-path if the site is served as a
// github.io project page.
describe.each(["/", "/morsekit/"])("routes under %s", (basename) => {
  const matchedRoute = (path: string) =>
    matchRoutes(APP_ROUTES, `${basename}${path}`, basename)?.at(-1)?.route;

  it("serves the privacy policy at the Play Console URL, with or without the slash", () => {
    expect(matchedRoute("privacy/")?.path).toBe(ROUTES.privacy);
    expect(matchedRoute("privacy")?.path).toBe(ROUTES.privacy);
  });

  it("serves home at the site root", () => {
    expect(matchedRoute("")?.index).toBe(true);
  });

  it("sends unknown paths to the catch-all", () => {
    expect(matchedRoute("nope")?.path).toBe("*");
  });
});
