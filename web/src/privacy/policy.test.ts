import { readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { PERMISSIONS, STORED_DATA } from "./policy";

const repo = fileURLToPath(new URL("../../../", import.meta.url));

function kotlinFiles(dir: string): string[] {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) return kotlinFiles(path);
    return entry.name.endsWith(".kt") ? [path] : [];
  });
}

describe("privacy policy", () => {
  it("lists exactly the permissions the app declares", () => {
    const manifest = readFileSync(
      join(repo, "androidApp/src/main/AndroidManifest.xml"),
      "utf8",
    );
    const declared = [
      ...manifest.matchAll(/<uses-permission\s+android:name="([^"]+)"/g),
    ].map((m) => m[1]);
    expect(declared.length).toBeGreaterThan(0);
    expect(PERMISSIONS.map((p) => p.name).sort()).toEqual(declared.sort());
  });

  it("describes every kind of data the app stores", () => {
    const sources = kotlinFiles(join(repo, "shared/src"));
    const prefixes = new Set(
      sources.flatMap((file) =>
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
