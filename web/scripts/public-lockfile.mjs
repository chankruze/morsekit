// Points package-lock.json at the public npm registry.
//
// npm records each package's download URL in the lockfile. On a machine whose npm uses a
// private mirror, those URLs point at the mirror, which CI (and anyone else) can't reach. The
// tarballs are the same, so only the host changes and the integrity hashes stay valid. Locally,
// npm still downloads through the configured mirror (npm's `replace-registry-host`).
//
//   node scripts/public-lockfile.mjs          rewrite in place (npm run lockfile:public)
//   node scripts/public-lockfile.mjs --check  exit 1 if any URL isn't on the public registry
import { readFileSync, writeFileSync } from "node:fs";

const PUBLIC = "https://registry.npmjs.org/";
const file = new URL("../package-lock.json", import.meta.url);
const check = process.argv.includes("--check");

const lock = JSON.parse(readFileSync(file, "utf8"));
const offending = [];
for (const [path, entry] of Object.entries(lock.packages)) {
  const url = entry.resolved;
  if (!url || url.startsWith(PUBLIC)) continue;
  // Registry tarball URLs end in "<name>/-/<file>.tgz", whatever the mirror's prefix.
  const match = /\/((?:@[^/]+\/)?[^/@]+)\/-\/([^/]+\.tgz)$/.exec(url);
  if (!match) throw new Error(`${path}: can't map ${url} to the public registry`);
  offending.push(path);
  entry.resolved = `${PUBLIC}${match[1]}/-/${match[2]}`;
}

if (check) {
  if (offending.length) {
    console.error(
      `package-lock.json has ${offending.length} URL(s) outside ${PUBLIC}, e.g. ${offending[0]}.\n` +
        "Run `npm run lockfile:public` in web/ and commit the result.",
    );
    process.exit(1);
  }
  console.log("package-lock.json uses only the public registry");
} else {
  writeFileSync(file, JSON.stringify(lock, null, 2) + "\n");
  console.log(`Rewrote ${offending.length} URL(s) to ${PUBLIC}`);
}
