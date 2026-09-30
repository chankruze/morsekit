import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import {
  APP_ID,
  PLAY_TESTING_URL,
  PLAY_URL,
  TESTING_GROUP_EMAIL,
  TESTING_GROUP_URL,
} from "@/constants/links";
import { repoPath } from "@/utils/repo-path.node";

const APP_BUILD_FILE = "androidApp/build.gradle.kts";

describe("links", () => {
  it("use the app's real application ID", () => {
    const build = readFileSync(repoPath(APP_BUILD_FILE), "utf8");
    expect(/applicationId = "([^"]+)"/.exec(build)?.[1]).toBe(APP_ID);
    expect(PLAY_URL).toContain(`id=${APP_ID}`);
    expect(PLAY_TESTING_URL).toMatch(new RegExp(`/apps/testing/${APP_ID}$`));
  });

  it("point the group page at the testers group", () => {
    const groupName = TESTING_GROUP_EMAIL.split("@")[0];
    expect(TESTING_GROUP_URL).toBe(`https://groups.google.com/g/${groupName}`);
  });
});
