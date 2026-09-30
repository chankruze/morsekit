# 14. The landing page (`web/`)

`web/` is the MorseKit website, published at **https://morsekit.geekofia.in/** (GitHub
Pages): a landing page and the **privacy policy** (`/privacy/`), built with **Vite**, **React 19** (with the React
Compiler) and **Tailwind CSS 4**. It's a separate npm project that sits next to the app, so the
app's layout is untouched: Gradle doesn't include it, and Android Studio excludes
`web/node_modules` and `web/dist` (the `idea { module { excludeDirs } }` block in the root
`build.gradle.kts`, applied on every Gradle sync).

```
web/
├── index.html, privacy/index.html   both load src/main.tsx (see Routing)
├── vite.config.ts                   @/ alias, version from ../version.properties, BASE_PATH
├── scripts/public-lockfile.mjs      keeps package-lock.json on the public registry
└── src/
    ├── main.tsx                     renderPage(<AppRouter />)
    ├── providers/                   app-routes.tsx (route table), app-router.tsx (RouterProvider)
    ├── layouts/site-layout.tsx      header, <Outlet />, footer; hero glow on home
    ├── components/                  site-header, site-footer, icon, text-link, section-heading…
    ├── constants/ types/ hooks/     routes, sections, links, titles; use-scroll-to-hash
    ├── styles/index.css             Tailwind, the logo palette (@theme), Space Grotesk
    ├── morse/                       the engine port: constants/ types/ utils/ services/
    └── screens/
        ├── home/                    screen.tsx + sections/ components/ hooks/ constants/ types/ utils/
        └── privacy/                 screen.tsx, layout.tsx + sections/ components/ constants/ types/
```

The code follows the repo's **`/structure`** command (`.claude/commands/structure.md`): kebab-case
file names, `@/` imports only, no barrel files, screens that only compose sections, logic in
hooks, magic values in `constants/`, list items wrapped in `React.memo`. Its React Native rules
(StyleSheet, FlashList) have no web equivalent; the styling rule maps to Tailwind theme tokens.

## Routing

**React Router** (v8) switches between the home page and `/privacy` without a reload. GitHub
Pages has no server-side routing, so a pure single-page app would answer a direct visit to
`/privacy/` with a 404. Instead, `privacy/index.html` is a real file that loads the same
app, and the router picks the screen from the URL:

| URL | Served by | Screen |
| --- | --- | --- |
| `/` | `index.html` | Home |
| `/privacy/` (the Play Console URL) | `privacy/index.html` | Privacy policy |
| anything else, while navigating in the app | | redirects home |

`basename` is Vite's `BASE_URL`, so the same routes work at `/` locally. React Router doesn't
scroll on navigation, so `useScrollToHash` scrolls to `#section` links (the header's) or to the
top of a new page. Page titles are React 19 `<title>` elements in each screen; React inserts them
before the static `<title>` in the HTML, which stays as the fallback without JavaScript.
`app-routes.test.ts` checks the matching under the Pages basename, with and without the
trailing slash.


## Sharing with the app instead of copying

| What | Source of truth | How the page stays in sync |
| --- | --- | --- |
| Version | `version.properties` | `vite.config.ts` reads `VERSION_NAME` at build time |
| Alphabet | `core/morse/MorseAlphabet.kt` | `morse/constants/alphabet.test.ts` parses the Kotlin file and fails if the tables differ |
| Codec rules | `MorseCodec`, `MorseNormalizer`, `MorseTokenizer` | Same behaviour, same tests: skipped/`�` characters, `•−_` accepted, word breaks on `/`, `\|`, newline or two spaces |
| Timing | `core/timing`, [note 8](08-audio-playback.md) | PARIS units (dot 1, dash 3, gaps 1/3/7), 600 Hz, 5 ms fades |
| Icons | `composeResources/drawable/ic_*.xml` | `components/icon.tsx` has the same path data |
| Logo, font | `ic_launcher-playstore.png`, `space_grotesk.ttf` | Copied once (rounded logo made with ImageMagick, as in [note 11](11-translator-ui.md)) |
| Links | `StoreListing.kt`, `AboutContent.kt` | Same URLs in `constants/links.ts` |

> **Why a port, not the Kotlin engine itself?** Kotlin can compile `shared` to JavaScript or
> WebAssembly, but that means a new Kotlin target, a JS bundle of the runtime and a build step
> joining Gradle and npm, all for ~150 lines of logic. The port is small, and the alphabet test
> catches the one kind of drift that matters. If the web ever needs more of the engine
> (e.g. a trainer), compiling `shared` to Wasm becomes worth it.

## The page

| Section | Notes |
| --- | --- |
| Hero | "MORSEKIT" in Morse, each element lit with real 10 WPM timing (`scheduleTones`), in the logo's colours; static when the user prefers reduced motion |
| Screenshots | Four phone shots (translator dark with the Transmit menu, translator light, reference, settings); a swipeable row on phones, a staggered grid on desktop |
| Try it | Text ⇄ Morse, swap, copy and **Play sound** (Web Audio). Editing or swapping stops the sound, like the app |
| Features | Translator, sound, flashlight, vibration, reference, settings |
| Privacy | Only claims that are true of the app: no internet permission, no ads or tracking, vibration is the only permission |
| Play button | "Coming soon to Google Play" until `PLAY_PUBLISHED = true` in `constants/links.ts` |

**Screenshots.** The page uses small copies of device screenshots, with the status bar (which
shows notifications and battery) cropped off, at 540 px wide as WebP: 57 KB for all four instead
of 1.8 MB.

```bash
magick Screenshot_….jpg -crop 1080x2314+0+100 +repage -resize 540x -strip -quality 80 translator-light.webp
```

**Web Audio instead of a pre-rendered buffer.** The app renders samples because Android and iOS
timers jitter ([note 8](08-audio-playback.md)). In the browser, `GainNode` automation is already
scheduled on the audio clock, so `playMorse` (`morse/services/play.ts`) sets each tone's fade-in and fade-out at exact times
and gets the same accuracy without rendering anything.

**Privacy of the page itself.** The font is self-hosted and there are no analytics or external
scripts, so, like the app, the page makes no third-party requests.

## The privacy policy

Play Console needs a public privacy policy URL: **https://morsekit.geekofia.in/privacy/**.
It's its own route with a real HTML file behind it (see Routing), so the URL is stable.

What it says, and where each claim comes from:

| Claim | Source in the app |
| --- | --- |
| No internet permission, no analytics, ads or accounts | `AndroidManifest.xml`; no such libraries |
| Only permission: vibration | `AndroidManifest.xml` (checked by a test) |
| What's stored on the device: settings, rating-prompt state, update-check state | `KEY_*` constants: `settings.*`, `review.*`, `update.*` (checked by a test) |
| Settings may be in the user's device backup | `android:allowBackup="true"` |
| Flashlight needs no camera access | `CameraManager.setTorchMode` |
| In-app updates and reviews are Google Play's | Play In-App Updates and Review libraries ([note 5](05-platform-services.md)) |
| The website has no cookies, analytics or third-party requests | Self-hosted font, no scripts |

> **Keeping it true.** `screens/privacy/constants/policy.test.ts` reads the manifest and every `const val KEY_... = "prefix.…"`
> in `shared/src`. Adding a permission, or storing a new kind of data under a new prefix, fails
> the web tests (and the deploy) until `policy.ts` describes it. When the policy's meaning
> changes, bump `EFFECTIVE_DATE`; the page's Git history is the public record of past versions.

## Deploying

`.github/workflows/web-deploy.yml` publishes to GitHub Pages on every push to `main` that touches
`web/`, `version.properties`, the app's Kotlin sources or its manifest (what the web tests read).
Pull requests only build and test.

| Step | Why |
| --- | --- |
| Lockfile check | `scripts/public-lockfile.mjs --check`: every download URL must be on `registry.npmjs.org` (see below) |
| `npm ci`, `npm test`, `npm run lint` | A failing test (e.g. the policy is out of date) stops the deploy |
| `configure-pages` → `BASE_PATH` | Pages says where the site lives (`/` on the custom domain); Vite prefixes every URL with it |
| `upload-pages-artifact`, `deploy-pages` | The Pages "GitHub Actions" source: no `gh-pages` branch |

> **Private npm mirrors (found on the first deploy).** npm writes each package's download URL
> into `package-lock.json`. On a machine whose npm uses a company mirror, new packages get the
> mirror's URLs, which GitHub's runners can't reach, so `npm ci` failed. `npm run lockfile:public`
> rewrites them to the public registry (same tarballs, so the integrity hashes still match), and
> locally npm keeps downloading through the mirror. CI checks this first, so the error says what
> to do. **After `npm install` on such a machine, run `npm run lockfile:public`.**

Pages was switched on once, with Actions as the source:
`gh api -X POST repos/chankruze/morsekit/pages -f build_type=workflow`. Its custom domain is
**`morsekit.geekofia.in`** (Settings › Pages), so the site is served from `/`.

> **Changing the domain needs a redeploy (found when it moved).** The base path is baked into
> the build: after `docs.geekofia.in/morsekit/` became `morsekit.geekofia.in`, the page asked for
> `/morsekit/assets/…` and got 404s. `configure-pages` reads the current setting, so re-running
> **Actions › Landing page › Run workflow** fixes it.

## Tests

`npm test` runs vitest (25 tests), each file next to the code it covers:

| File | Covers |
| --- | --- |
| `morse/constants/alphabet.test.ts` | The table equals the Kotlin alphabet (54 characters), no duplicate codes |
| `morse/utils/*.test.ts` | Encode/decode, normalization, word splitting, glyphs, round trip; tone timing (60 ms unit at 20 WPM, 1/3/7 gaps, PARIS = 50 units) |
| `screens/home/utils/build-signal-marks.test.ts` | The hero signal gives each element its own tone, in order |
| `screens/privacy/constants/policy.test.ts` | The policy lists exactly the manifest's permissions and every stored-data prefix |
| `providers/app-routes.test.ts` | Under `/` and a sub-path: `privacy/` (with or without `/`) is the privacy route; the root is home |
