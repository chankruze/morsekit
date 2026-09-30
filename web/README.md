# MorseKit landing page

The website for MorseKit, at https://morsekit.geekofia.in/: a Vite + React + Tailwind CSS
landing page with a small live translator (same alphabet and timing as the app), and the
privacy policy at `/privacy/`. See
[docs/14-landing-page.md](../docs/14-landing-page.md) for how it's put together.

```bash
cd web
npm install
npm run dev       # http://localhost:5173
npm test          # vitest: engine, routes; alphabet and privacy facts match the app's source
npm run lint
npm run build     # → web/dist/
```

- The version in the footer comes from the repo's `version.properties` at build time.
- Until `PLAY_PUBLISHED` in `src/constants/links.ts` is `true`, the Google Play button leads to the
  "Join the beta" steps (Google Group `geekofia@googlegroups.com` → opt in → install).
- Code layout follows `/structure` (`.claude/commands/structure.md`).
- `BASE_PATH=/morsekit/ npm run build` builds for a sub-path; the deploy sets it from Pages (`/` on
  the custom domain).
- After `npm install` behind a private npm mirror, run `npm run lockfile:public` (CI can only
  reach the public registry).
- Pushing to `main` deploys (`.github/workflows/web-deploy.yml`).
- If the app gains a permission or stores a new kind of data, update `src/screens/privacy/constants/policy.ts`
  (the tests will say so).
