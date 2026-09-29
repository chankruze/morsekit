# 7. Versioning and builds

## One version, two platforms

MorseKit uses [Semantic Versioning](https://semver.org): `MAJOR.MINOR.PATCH`, starting at
`0.0.1`. Both apps read the version from **one file** at the repo root:

```properties
// version.properties
VERSION_NAME=0.0.1
VERSION_CODE=1
```

```mermaid
flowchart LR
    VP["version.properties<br/>VERSION_NAME, VERSION_CODE"]
    Bump["scripts/bump-version.sh"] -->|"rewrites"| VP
    VP -->|"providers.fileContents<br/>+ java.util.Properties"| Gradle["androidApp/build.gradle.kts"]
    VP -->|"include directive"| XC["iosApp/Configuration/Config.xcconfig"]
    Gradle --> AV["versionName = 0.0.1<br/>versionCode = 1"]
    XC --> IV["MARKETING_VERSION = 0.0.1<br/>CURRENT_PROJECT_VERSION = 1"]
    AV --> Files["MorseKit-v0.0.1-1-release.apk / .aab"]
```

| Field | Meaning | Android | iOS |
| --- | --- | --- | --- |
| `VERSION_NAME` | The version users see, SemVer | `versionName` | `MARKETING_VERSION` → `CFBundleShortVersionString` |
| `VERSION_CODE` | A build number that only ever goes up | `versionCode` | `CURRENT_PROJECT_VERSION` → `CFBundleVersion` |

> **Why one file works for both.** `.properties` files and Xcode `.xcconfig` files share the same
> `KEY=value` syntax, so Xcode can `#include` the file directly. That's also why comments in it
> use `//`: xcconfig understands `//`, and Java `Properties` just reads `//` lines as an unused
> key. A `#` comment would break the Xcode include.

> **Why a separate build number?** Stores reject an upload whose build number has been used
> before, even for the same version. iOS TestFlight often needs several uploads of `0.1.0`, and
> `VERSION_CODE` lets you do that (`bump-version.sh build`). Android's `versionCode` must also
> increase with every Play upload.

### Rules the build enforces

`androidApp/build.gradle.kts` fails fast if the file is wrong:

| Check | Example that fails |
| --- | --- |
| `VERSION_NAME` is exactly `MAJOR.MINOR.PATCH`, with no leading zeros | `1.0`, `01.2.3`, `1.0.0-beta` |
| `VERSION_CODE` is a positive integer | `0`, `abc` |

Pre-release tags like `-beta.1` aren't allowed, because iOS requires `CFBundleShortVersionString`
to be plain numbers.

## Bumping the version

```bash
scripts/bump-version.sh patch            # 0.0.1 (1) -> 0.0.2 (2)   bug fixes
scripts/bump-version.sh minor            # 0.0.2 (2) -> 0.1.0 (3)   new features
scripts/bump-version.sh major            # 0.1.0 (3) -> 1.0.0 (4)   breaking changes / 1.0 launch
scripts/bump-version.sh build            # 1.0.0 (4) -> 1.0.0 (5)   same version, new upload
scripts/bump-version.sh set 2.3.0        # 1.0.0 (5) -> 2.3.0 (6)   explicit
scripts/bump-version.sh patch --dry-run  # preview only
```

| Command | `VERSION_NAME` | `VERSION_CODE` |
| --- | --- | --- |
| `patch` | `x.y.Z+1` | +1 |
| `minor` | `x.Y+1.0` | +1 |
| `major` | `X+1.0.0` | +1 |
| `build` | unchanged | +1 |
| `set A.B.C` | `A.B.C` | +1 |

The script only edits `version.properties`; it prints the suggested commit and tag, e.g.
`chore(release): v0.0.2` and `git tag v0.0.2`. While the major version is `0`, SemVer treats the
API as unstable, which suits an app in early development.

## Build types and artifact names

There are no product flavors (MorseKit is offline, so there are no environments to switch), just
the two default build types:

| Build type | Application ID | Launcher name | Minified | Signed with |
| --- | --- | --- | --- | --- |
| `debug` | `in.geekofia.morsekit.debug` | MorseKit Debug | no | debug key |
| `release` | `in.geekofia.morsekit` | MorseKit | **yes (R8 + resource shrinking)** | the release key, via `keystore.properties` (unsigned if that file is absent) |

The `.debug` suffix lets both builds be installed side by side on one device. The different
launcher name comes from `androidApp/src/debug/res/values/strings.xml`, which overrides
`app_name` from `src/main` for debug builds only.

> **Concept: source-set overlays.** Android merges resources from `src/main` with the build
> type's source set (`src/debug`, `src/release`). A resource with the same name in the more
> specific source set wins, with no Gradle configuration needed.

> **Lesson learned.** Before this label existed, an old `in.geekofia.morsekit` install (from
> before the `.debug` suffix) sat next to the new debug app, both labelled "MorseKit". Opening
> the wrong icon showed stale screens. If a change seems missing on a device, check
> `adb shell pm list packages | grep morsekit` first.

### Named APK / AAB

AGP writes outputs with generic names (`androidApp-release.apk`). After each build, a copy is
placed in `androidApp/build/dist/` named `<app>-v<VERSION_NAME>-<VERSION_CODE>-<variant>`:

| Command | Output |
| --- | --- |
| `./gradlew :androidApp:assembleDebug` | `androidApp/build/dist/MorseKit-v0.0.1-1-debug.apk` |
| `./gradlew :androidApp:assembleRelease` | `androidApp/build/dist/MorseKit-v0.0.1-1-release.apk` |
| `./gradlew :androidApp:bundleRelease` | `androidApp/build/dist/MorseKit-v0.0.1-1-release.aab` |

If product flavors are added later, the variant name (e.g. `prodRelease`) appears in the file name
automatically.

```mermaid
sequenceDiagram
    participant You
    participant Gradle
    participant AGP as AGP tasks
    participant Copy as copyReleaseNamedApk
    You->>Gradle: ./gradlew assembleRelease
    Gradle->>AGP: compile, package
    AGP-->>Gradle: build/outputs/apk/release/androidApp-release.apk
    Gradle->>Copy: finalizedBy
    Copy-->>You: build/dist/MorseKit-v0.0.1-1-release.apk
```

### How it's wired (Gradle concepts)

```kotlin
// Simplified from androidApp/build.gradle.kts (Variant = "Release", baseName = "MorseKit-v0.0.1-1-release")
androidComponents {
    onVariants { variant ->
        val copyApk = tasks.register<Copy>("copy${Variant}NamedApk") {
            from(variant.artifacts.get(SingleArtifact.APK)) { include("*.apk"); rename { "$baseName.apk" } }
            into(layout.buildDirectory.dir("dist"))
        }
        tasks.matching { it.name == "assemble$Variant" }.configureEach { finalizedBy(copyApk) }
    }
}
```

| Concept | Explanation |
| --- | --- |
| `androidComponents.onVariants` | AGP's current **Variant API**: a callback for each variant (`debug`, `release`). AGP 9 removed the older `applicationVariants.all { outputFileName = ... }` hook that many older projects (and the React Native reference project) use |
| `variant.artifacts.get(SingleArtifact.APK)` | A lazy `Provider` of the APK folder. Using it as a `from(...)` input automatically makes the copy task depend on the packaging task |
| `SingleArtifact.BUNDLE` | Same, for the `.aab` |
| `finalizedBy` | Runs the copy right after `assemble<Variant>` / `bundle<Variant>`, so the normal commands produce named files |
| `providers.fileContents(...)` | Reads `version.properties` in a way the **configuration cache** tracks; editing the file correctly invalidates the cache |

## Shrinking release builds (R8)

Release builds run **R8** (`isMinifyEnabled = true`), which removes unused code, inlines and
optimises, and shortens class and member names. Then **resource shrinking**
(`isShrinkResources = true`) drops Android resources nothing references. Debug builds are left
alone.

| | Before | After | Change |
| --- | --- | --- | --- |
| Release APK | 26.3 MB | 3.06 MB | −88% |
| Release AAB (what Play downloads from) | 9.35 MB | 4.30 MB | −54% |
| Code (`classes.dex`, uncompressed) | 25.4 MB | 2.54 MB | −90% |

Almost the whole APK was code (Compose, Kotlin and the other libraries), which is exactly what R8
removes.

| Topic | Notes |
| --- | --- |
| Keep rules | Libraries ship their own (consumer rules); MorseKit's `proguard-rules.pro` is empty because the app uses no reflection. Add a rule there only if a release build shows a missing class or method at runtime |
| Compose resources | Loaded from `assets/composeResources/` through generated accessors, so resource shrinking (which works on `res/`) doesn't touch fonts, icons or the logo |
| Enums saved by name | `ThemeMode` and the saved tab are stored as `name`; R8 keeps enum names' values, so saved settings still load |
| Crash stack traces | R8 renames classes, so release crashes show short names. `androidApp/build/outputs/mapping/release/mapping.txt` maps them back: keep it for every published build (Play Console can store it: *App bundle explorer › Downloads › deobfuscation file*) |
| Testing | Minification problems only show at runtime, so test a **signed** release build on a device before publishing |

## Release signing

The release key never goes into git. Gradle reads the path and credentials from
**`keystore.properties`** at the repo root, which is git-ignored along with `*.jks` and
`*.keystore`:

```properties
storeFile=~/Desktop/MorseKit.jks     # absolute, relative to the repo root, or ~ for home
storePassword=...
keyAlias=...
keyPassword=...
```

`keystore.properties.example` (committed) documents the keys. Behaviour:

| `keystore.properties` | Release build |
| --- | --- |
| Absent (fresh clone) | Builds, **unsigned** |
| Present but a value is missing | Fails: `keystore.properties: 'storePassword' is missing or empty` |
| Complete | Signed with the release key, so `assembleRelease` / `bundleRelease` are ready to upload |

It's read with `providers.fileContents`, so editing the file invalidates the configuration cache.
In CI, the release workflow writes this file from repository secrets
([note 13](13-ci-release.md)).

> **Keep the keystore safe.** Google Play ties the app to its signing key; with Play App Signing,
> this is the *upload* key. Back up `MorseKit.jks` and its passwords somewhere other than this
> machine (a password manager). Losing the upload key means asking Google to reset it; without
> Play App Signing, a lost key means you can never update the app.

