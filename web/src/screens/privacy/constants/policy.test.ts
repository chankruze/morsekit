import { readdirSync, readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { PERMISSIONS, STORED_DATA } from "@/screens/privacy/constants/policy";
import { repoPath } from "@/utils/repo-path.node";

const MANIFEST = "androidApp/src/main/AndroidManifest.xml";
const SHARED_SOURCES = "shared/src";

const kotlinFiles = (dir: string): string[] =>
  readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) return kotlinFiles(path);
    return entry.name.endsWith(".kt") ? [path] : [];
  });

describe("privacy policy", () => {
  it("lists exactly the permissions the app declares", () => {
    const manifest = readFileSync(repoPath(MANIFEST), "utf8");
    const declared = [
      ...manifest.matchAll(/<uses-permission\s+android:name="([^"]+)"/g),
    ].map((m) => m[1]);
    expect(declared.length).toBeGreaterThan(0);
    expect(PERMISSIONS.map((p) => p.name).sort()).toEqual(declared.sort());
  });

  it("describes every kind of data the app stores", () => {
    const prefixes = new Set(
      kotlinFiles(repoPath(SHARED_SOURCES)).flatMap((file) =>
        [
          ...readFileSync(file, "utf8").matchAll(
            /const val KEY_\w+\s*=\s*"([a-zA-Z]+)\./g,
          ),
        ].map((m) => m[1]),
      ),
    );
    expect(prefixes.size).toBeGreaterThan(0);
    expect(STORED_DATA.map((d) => d.prefix).sort()).toEqual(
      [...prefixes].sort(),
    );
  });
});
