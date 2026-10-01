This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Architecture

All code lives under the `in.geekofia.morsekit` package in `shared/src`:

| Package | Contents |
| --- | --- |
| `core/model` | Immutable Morse models (`MorseLetter`, `MorseWord`, `MorseMessage`, `TranslationDirection`) |
| `core/morse` | Pure-Kotlin engine: `MorseAlphabet`, `MorseNormalizer`, `MorseTokenizer`, `MorseCodec` |
| `core/timing` | `MorseTiming` (WPM → unit length) and `MorseMessage.toSignals()` for playback |
| `feature/<name>` | One package per feature: screen composables + ViewModel + UI state |
| `navigation` | `TopLevelDestination` (bottom bar tabs) |
| `platform` | Interfaces for platform capabilities (`ClipboardService`, `ShareService`), implemented in `androidMain` / `iosMain` |
| `ui/theme`, `ui/components` | `MorseKitTheme` (light/dark/system) and reusable composables |

Data flows one way: composable → ViewModel → `MorseCodec` → UI state → composable.
`core` has no Compose or platform dependencies. Platform services are constructed in
`MainActivity` / `MainViewController` and passed to `App(platformServices)` explicitly.

Engine and ViewModel tests live in `commonTest` and run on both Android (host JVM) and iOS.

For a detailed walkthrough (diagrams of the Morse encode/decode flow, timing, Compose state, and
the KMP concepts used), see the [learning notes in `docs/`](docs/README.md).

### Roadmap

Progress on the MorseKit backlog. Status: ✅ Done · 🚧 In Progress · ⬜ Todo.

**72 done · 7 in progress · 1 todo** (80 tasks)

| Feature | Done | In Progress | Todo |
| --- | --- | --- | --- |
| Project Foundation | 6 | 0 | 0 |
| Morse Engine | 6 | 0 | 0 |
| Translator | 9 | 0 | 0 |
| Morse Reference | 4 | 0 | 0 |
| Audio | 4 | 0 | 0 |
| Flashlight | 6 | 0 | 0 |
| Vibration | 3 | 0 | 0 |
| Morse Trainer | 6 | 0 | 0 |
| Tap Morse | 5 | 0 | 0 |
| History | 3 | 0 | 0 |
| Settings | 7 | 0 | 0 |
| Platform | 2 | 1 | 0 |
| Quality | 3 | 1 | 0 |
| Release | 8 | 5 | 1 |

#### All tasks

| Feature | Area | Task | Priority | Status |
| --- | --- | --- | --- | --- |
| Project Foundation | Project setup | Configure package/application IDs, app name MorseKit, and project metadata | P0 | ✅ Done |
| Project Foundation | Architecture | Define shared KMP modules/packages for core, UI, and platform-specific code | P0 | ✅ Done |
| Project Foundation | Theme | Create MorseKit Material 3 theme with light/dark/system modes | P0 | ✅ Done |
| Project Foundation | Navigation | Set up Compose Multiplatform navigation structure | P0 | ✅ Done |
| Project Foundation | Testing | Set up unit-test structure for shared Morse logic | P0 | ✅ Done |
| Project Foundation | Back navigation | Navigation 3 back stack with Material back rules, kept tab state and press-back-again to exit | P1 | ✅ Done |
| Morse Engine | Alphabet | Implement International Morse alphabet mapping | P0 | ✅ Done |
| Morse Engine | Encoder | Implement text-to-Morse conversion | P0 | ✅ Done |
| Morse Engine | Decoder | Implement Morse-to-text conversion | P0 | ✅ Done |
| Morse Engine | Validation | Handle unsupported characters and invalid Morse sequences | P0 | ✅ Done |
| Morse Engine | Formatting | Support word separators, character spacing, and normalized Morse output | P0 | ✅ Done |
| Morse Engine | Tests | Add comprehensive encoder/decoder unit tests | P0 | ✅ Done |
| Translator | Text to Morse | Build text input and live Morse output | P0 | ✅ Done |
| Translator | Morse to Text | Build Morse input and decoded text output | P0 | ✅ Done |
| Translator | Swap | Add one-tap Text ↔ Morse mode switching | P0 | ✅ Done |
| Translator | Clipboard | Copy input/output to clipboard | P0 | ✅ Done |
| Translator | Share | Share translated content using platform share APIs | P0 | ✅ Done |
| Translator | Clear | Clear current input/output | P0 | ✅ Done |
| Translator | Redesign | One-screen layout: direction bar, in-card copy/share, Transmit FAB speed dial with WPM stepper | P1 | ✅ Done |
| Translator | Share message | Share a friendly puzzle (Text → Morse) or reveal (Morse → Text) message with the store link | P2 | ✅ Done |
| Translator | Prosign decoding | Decode prosign codes in the translator (...---... → <SOS>) and encode <SOS> back as one letter | P3 | ✅ Done |
| Morse Reference | Alphabet chart | Display A-Z Morse reference chart | P0 | ✅ Done |
| Morse Reference | Numbers | Display 0-9 Morse codes | P1 | ✅ Done |
| Morse Reference | Punctuation | Display common punctuation and prosigns | P1 | ✅ Done |
| Morse Reference | Search | Search/filter Morse characters | P2 | ✅ Done |
| Audio | Morse audio | Convert Morse symbols into audible tones | P1 | ✅ Done |
| Audio | WPM | Add adjustable Morse transmission speed | P1 | ✅ Done |
| Audio | Frequency | Allow tone frequency adjustment | P2 | ✅ Done |
| Audio | Playback controls | Play, stop, and replay Morse audio | P1 | ✅ Done |
| Flashlight | Transmission | Transmit Morse using device flashlight/torch | P1 | ✅ Done |
| Flashlight | Timing | Implement correct dot/dash and gap timing | P1 | ✅ Done |
| Flashlight | WPM | Use configurable transmission speed | P1 | ✅ Done |
| Flashlight | Safety | Add clear controls and warning for flashing light | P1 | ✅ Done |
| Flashlight | Screen timeout | Keep the screen on while transmitting so long, slow messages aren't cut off when the screen turns off | P3 | ✅ Done |
| Flashlight | Rotation | Keep flashing across screen rotation instead of stopping | P3 | ✅ Done |
| Vibration | Transmission | Transmit Morse using device vibration/haptics | P1 | ✅ Done |
| Vibration | Timing | Implement Morse timing for vibration patterns | P1 | ✅ Done |
| Vibration | WPM | Use configurable transmission speed | P2 | ✅ Done |
| Morse Trainer | Character mode | Show Morse and ask user to identify the character | P2 | ✅ Done |
| Morse Trainer | Reverse mode | Show a character and key its Morse (reusing the Tap decoder: Timing or Buttons) | P2 | ✅ Done |
| Morse Trainer | Scoring | Track correct answers and accuracy | P2 | ✅ Done |
| Morse Trainer | Progression | Gradually introduce new characters | P2 | ✅ Done |
| Morse Trainer | Session | Practice sessions of 10, 20 or 50 questions with a summary (accuracy, misses, unlocks) | P3 | ✅ Done |
| Morse Trainer | Koch order check | Koch order matches LCWO's ($kochchar in its source, all 41 characters in order) | P2 | ✅ Done |
| Tap Morse | Tap input | Tap to enter dots | P2 | ✅ Done |
| Tap Morse | Long press | Long press to enter dashes | P2 | ✅ Done |
| Tap Morse | Character detection | Convert tap sequences into characters | P2 | ✅ Done |
| Tap Morse | Haptic feedback | Provide feedback while tapping | P3 | ✅ Done |
| Tap Morse | Button mode | Dot, Dash, Next letter and Space buttons with a Timing \| Buttons switch, plus a live letter preview | P2 | ✅ Done |
| History | Recent translations | Keep translations that were used (copied, shared, sent), locally and out of backups, with a Save history switch | P2 | ✅ Done |
| History | Favorites | Allow users to favorite frequently used messages | P3 | ✅ Done |
| History | Delete | Delete individual or all history items | P3 | ✅ Done |
| Settings | Theme | Light, dark, and system theme selection | P1 | ✅ Done |
| Settings | WPM | Configure default Morse transmission speed | P1 | ✅ Done |
| Settings | Audio | Configure default tone settings | P2 | ✅ Done |
| Settings | About | Add app version, privacy information, and open-source/license information | P1 | ✅ Done |
| Settings | Reset | Reset settings to defaults, with confirmation | P3 | ✅ Done |
| Settings | Credits | Developer credits and links | P3 | ✅ Done |
| Settings | About privacy text | Say settings may be included in the device backup (android:allowBackup), matching the privacy policy | P1 | ✅ Done |
| Platform | Android | Implement Android-specific torch, vibration, audio, clipboard, and sharing integrations | P0 | ✅ Done |
| Platform | iOS | Implement iOS-specific torch, haptics, audio, clipboard, and sharing integrations | P0 | 🚧 In Progress |
| Platform | Permissions | Add only required platform permissions and document their purpose | P0 | ✅ Done |
| Quality | Accessibility | Content descriptions, semantic labels, scalable text and touch targets (code pass done; check with TalkBack on a device) | P1 | 🚧 In Progress |
| Quality | Error states | Handle empty, invalid, and unsupported input gracefully | P1 | ✅ Done |
| Quality | Offline | Ensure all core functionality works without network access | P0 | ✅ Done |
| Quality | Backup check | History confirmed excluded from Android backup (Android 16 test: settings and trainer level restored, History not); Tap, Learn and History tested on a phone | P1 | ✅ Done |
| Release | App icon | Create MorseKit launcher/app icon | P1 | 🚧 In Progress |
| Release | Store assets | Screenshots taken (four are on the website); Play listing text and App Store assets left | P2 | 🚧 In Progress |
| Release | Build | Signed Android release builds and CI done; iOS archive left (needs Xcode) | P1 | 🚧 In Progress |
| Release | CI | GitHub Actions workflow: build signed APK/AAB and mapping file for GitHub releases | P1 | ✅ Done |
| Release | Play publishing | Upload GitHub releases to Play internal testing with release notes; approved promotion to production | P1 | ✅ Done |
| Release | Play setup | Play Console app and closed test created (build 5 uploaded); left: service account, PLAY_SERVICE_ACCOUNT_JSON secret, production environment | P1 | 🚧 In Progress |
| Release | Store links | Check the Play Store link in shared messages once published; add an App Store link for iOS recipients | P2 | ⬜ Todo |
| Release | Ratings | Ask for a store rating at good moments and add a Rate MorseKit banner | P2 | ✅ Done |
| Release | In-app updates | Android: Play In-App Updates (flexible, immediate when urgent), daily check with 7-day snooze, Check for updates in Settings | P2 | ✅ Done |
| Release | Shrinking | R8 minification and resource shrinking for release builds | P2 | ✅ Done |
| Release | Landing page | Website (web/, Vite + React + Tailwind) with live translator and screenshots, deployed to GitHub Pages | P2 | ✅ Done |
| Release | Privacy policy | Public privacy policy page for Play Console, checked against the app's permissions and stored data | P1 | ✅ Done |
| Release | Beta signup | Join the closed test from the website: Google Group testers list, opt-in and install steps | P1 | ✅ Done |
| Release | Closed test upload | v1.1.1 (build 7, which includes build 6's Tap, Learn and History) released on GitHub and built by CI; left: upload its AAB to the closed test | P1 | 🚧 In Progress |

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.
- Landing page: `cd web && npm install && npm run dev` (see [web/README.md](web/README.md)).
  Published at https://morsekit.geekofia.in/, with the
  [privacy policy](https://morsekit.geekofia.in/privacy/).

### Versioning and release artifacts

The version (SemVer) and build number live in [`version.properties`](version.properties) and are
shared by Android and iOS. Bump them with `scripts/bump-version.sh <major|minor|patch|build>`.

Builds also copy their output to `androidApp/build/dist/` with a descriptive name:

- `./gradlew :androidApp:assembleRelease` → `MorseKit-v0.0.1-1-release.apk`
- `./gradlew :androidApp:bundleRelease` → `MorseKit-v0.0.1-1-release.aab`

See [docs/07-versioning-and-builds.md](docs/07-versioning-and-builds.md) for details.

Publishing a GitHub release runs [`android-release.yml`](.github/workflows/android-release.yml),
which attaches the signed APK, AAB and R8 mapping file to the release and uploads the AAB to
Google Play's internal testing track. [`play-promote.yml`](.github/workflows/play-promote.yml)
then promotes it to production after your approval. See
[docs/13-ci-release.md](docs/13-ci-release.md) for the one-time setup.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…