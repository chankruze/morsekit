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

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Versioning and release artifacts

The version (SemVer) and build number live in [`version.properties`](version.properties) and are
shared by Android and iOS. Bump them with `scripts/bump-version.sh <major|minor|patch|build>`.

Builds also copy their output to `androidApp/build/dist/` with a descriptive name:

- `./gradlew :androidApp:assembleRelease` → `MorseKit-v0.0.1-1-release.apk`
- `./gradlew :androidApp:bundleRelease` → `MorseKit-v0.0.1-1-release.aab`

See [docs/07-versioning-and-builds.md](docs/07-versioning-and-builds.md) for details.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…