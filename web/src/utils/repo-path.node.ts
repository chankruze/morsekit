import { fileURLToPath } from "node:url";

const REPO_ROOT = new URL("../../../", import.meta.url);

/** A file in the MorseKit repo (outside web/), for tests that check the app's source. */
export const repoPath = (relativePath: string): string =>
  fileURLToPath(new URL(relativePath, REPO_ROOT));
