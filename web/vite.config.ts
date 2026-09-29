import react, { reactCompilerPreset } from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";
import tailwindcss from "@tailwindcss/vite";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vite";

/** The app's version.properties (repo root), so the page always shows the current version. */
function appVersion(): string {
  const text = readFileSync(
    new URL("../version.properties", import.meta.url),
    "utf8",
  );
  const name = /^VERSION_NAME=(.+)$/m.exec(text)?.[1]?.trim();
  if (!name) throw new Error("version.properties: VERSION_NAME not found");
  return name;
}

// https://vite.dev/config/
export default defineConfig({
  // GitHub Pages serves project sites from /<repo>/; the deploy workflow sets BASE_PATH.
  base: process.env.BASE_PATH ?? "/",
  define: {
    __APP_VERSION__: JSON.stringify(appVersion()),
  },
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] }),
    tailwindcss(),
  ],
  resolve: {
    alias: { "@": fileURLToPath(new URL("src", import.meta.url)) },
  },
  build: {
    // Two pages: the landing page and /privacy/ (the URL given to Play Console).
    rolldownOptions: {
      input: {
        main: fileURLToPath(new URL("index.html", import.meta.url)),
        privacy: fileURLToPath(new URL("privacy/index.html", import.meta.url)),
      },
    },
  },
});
