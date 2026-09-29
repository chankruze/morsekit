# 14. The landing page (`web/`)

`web/` is the MorseKit website, published at **https://docs.geekofia.in/morsekit/** (GitHub
Pages): a landing page and the **privacy policy** (`/privacy/`), built with **Vite**, **React 19** (with the React
Compiler) and **Tailwind CSS 4**. It's a separate npm project that sits next to the app, so the
app's layout is untouched: Gradle doesn't include it, and Android Studio excludes
`web/node_modules` and `web/dist` (the `idea { module { excludeDirs } }` block in the root
`build.gradle.kts`, applied on every Gradle sync).

```
web/
├── index.html              landing page: title, description, icons
├── privacy/index.html      privacy policy page (a second Vite entry)
├── vite.config.ts          reads ../version.properties → __APP_VERSION__; BASE_PATH
├── public/                 favicon.png, apple-touch-icon.png (from the Play Store icon)
└── src/
    ├── root.tsx            the page: header, hero, Try it, features, privacy, footer
    ├── links.ts            Play / GitHub / developer links, PLAY_PUBLISHED
    ├── index.css           Tailwind, the logo palette (@theme), Space Grotesk
    ├── components/         SiteChrome (shared header/footer), Translator, MorseSignal, icons
    ├── privacy/            policy.ts (facts), PrivacyPolicy.tsx (text), main.tsx
    └── morse/              alphabet, codec, audio: a TypeScript port of the app's engine
```

## Sharing with the app instead of copying

| What | Source of truth | How the page stays in sync |
| --- | --- | --- |
| Version | `version.properties` | `vite.config.ts` reads `VERSION_NAME` at build time |
| Alphabet | `core/morse/MorseAlphabet.kt` | `alphabet.test.ts` parses the Kotlin file and fails if the tables differ |
| Codec rules | `MorseCodec`, `MorseNormalizer`, `MorseTokenizer` | Same behaviour, same tests: skipped/`�` characters, `•−_` accepted, word breaks on `/`, `\|`, newline or two spaces |
| Timing | `core/timing`, [note 8](08-audio-playback.md) | PARIS units (dot 1, dash 3, gaps 1/3/7), 600 Hz, 5 ms fades |
| Icons | `composeResources/drawable/ic_*.xml` | `components/icons.tsx` has the same path data |
| Logo, font | `ic_launcher-playstore.png`, `space_grotesk.ttf` | Copied once (rounded logo made with ImageMagick, as in [note 11](11-translator-ui.md)) |
| Links | `StoreListing.kt`, `AboutContent.kt` | Same URLs in `links.ts` |

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
| Play button | "Coming soon to Google Play" until `PLAY_PUBLISHED = true` in `links.ts` |

**Screenshots.** The page uses small copies of device screenshots, with the status bar (which
shows notifications and battery) cropped off, at 540 px wide as WebP: 57 KB for all four instead
of 1.8 MB.

```bash
magick Screenshot_….jpg -crop 1080x2314+0+100 +repage -resize 540x -strip -quality 80 translator-light.webp
```

**Web Audio instead of a pre-rendered buffer.** The app renders samples because Android and iOS
timers jitter ([note 8](08-audio-playback.md)). In the browser, `GainNode` automation is already
scheduled on the audio clock, so `playMorse` sets each tone's fade-in and fade-out at exact times
and gets the same accuracy without rendering anything.

**Privacy of the page itself.** The font is self-hosted and there are no analytics or external
scripts, so, like the app, the page makes no third-party requests.

## The privacy policy

Play Console needs a public privacy policy URL: **https://docs.geekofia.in/morsekit/privacy/**.
It's a real page (`privacy/index.html`, a second entry in `vite.config.ts`), not a section of the
landing page, so the URL is stable and the page loads nothing else.

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

> **Keeping it true.** `policy.test.ts` reads the manifest and every `const val KEY_... = "prefix.…"`
> in `shared/src`. Adding a permission, or storing a new kind of data under a new prefix, fails
> the web tests (and the deploy) until `policy.ts` describes it. When the policy's meaning
> changes, bump `EFFECTIVE_DATE`; the page's Git history is the public record of past versions.

## Deploying

`.github/workflows/web-deploy.yml` publishes to GitHub Pages on every push to `main` that touches
`web/`, `version.properties`, the app's Kotlin sources or its manifest (what the web tests read).
Pull requests only build and test.

| Step | Why |
| --- | --- |
| `npm ci`, `npm test`, `npm run lint` | A failing test (e.g. the policy is out of date) stops the deploy |
| `configure-pages` → `BASE_PATH` | The site lives under `/morsekit/`; Vite prefixes every URL with it |
| `upload-pages-artifact`, `deploy-pages` | The Pages "GitHub Actions" source: no `gh-pages` branch |

Pages was switched on once, with Actions as the source:
`gh api -X POST repos/chankruze/morsekit/pages -f build_type=workflow`. The account's Pages
custom domain (`docs.geekofia.in`) applies to this project site too.

## Tests

`npm test` runs vitest (21 tests):

| File | Covers |
| --- | --- |
| `alphabet.test.ts` | The table equals the Kotlin alphabet (54 characters), no duplicate codes |
| `codec.test.ts` | Encode/decode, case and quote normalization, whitespace, unsupported and unreadable input, round trip of every character |
| `policy.test.ts` | The policy lists exactly the manifest's permissions and every stored-data prefix |
| `audio.test.ts` | 60 ms unit at 20 WPM, element/letter/word gaps, PARIS = 50 units, malformed tokens skipped |
