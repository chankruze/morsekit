# 5. Platform services

Shared code sometimes needs things only the operating system can do: copy to the clipboard,
open the share sheet, and later turn on the torch, vibrate, or play a tone. The rule in
MorseKit is:

> **`commonMain` depends on interfaces. `androidMain` and `iosMain` provide the
> implementations. The app entry points connect them.**

## The pieces

```mermaid
classDiagram
    direction TB
    class ClipboardService {
        <<fun interface>>
        +copyText(text)
    }
    class ShareService {
        <<fun interface>>
        +shareText(text)
    }
    class KeyValueStore {
        <<interface>>
        +getString(key) String?
        +putString(key, value)
        +getInt(key) Int?
        +putInt(key, value)
    }
    class PlatformServices {
        +clipboard: ClipboardService
        +share: ShareService
        +keyValueStore: KeyValueStore
        +appInfo: AppInfo
    }
    class AndroidClipboardService {
        androidMain
    }
    class AndroidShareService {
        androidMain
    }
    class IosClipboardService {
        iosMain
    }
    class IosShareService {
        iosMain
    }
    PlatformServices --> ClipboardService
    PlatformServices --> ShareService
    PlatformServices --> KeyValueStore
    ClipboardService <|.. AndroidClipboardService
    ShareService <|.. AndroidShareService
    ClipboardService <|.. IosClipboardService
    ShareService <|.. IosShareService
```

| Interface (`commonMain`) | Android (`androidMain`) | iOS (`iosMain`) |
| --- | --- | --- |
| `ClipboardService.copyText` | `ClipboardManager.setPrimaryClip(ClipData.newPlainText(...))` | `UIPasteboard.generalPasteboard.string = text` |
| `ShareService.shareText` | `Intent.ACTION_SEND` in `Intent.createChooser(...)` | `UIActivityViewController` presented from the top-most view controller |
| `KeyValueStore` | `SharedPreferences` (file `morsekit`) | `NSUserDefaults.standardUserDefaults` |
| `AppInfo` (data, not a service) | `PackageManager.getPackageInfo` → `versionName`, `longVersionCode` | `NSBundle.mainBundle` → `CFBundleShortVersionString`, `CFBundleVersion` |

`InMemoryKeyValueStore` (in `commonMain`) is a third implementation, used by tests and previews.

> **Why not DataStore or `multiplatform-settings`?** Both are good libraries, but three
> preferences don't need them. The interface is four methods, each platform implementation is
> about 15 lines, and no dependency is added. If storage needs grow (history, favorites), a real
> database would replace this rather than a bigger key-value library.

Factory functions build the bundle for each platform:

- `androidPlatformServices(context)`: `shared/src/androidMain/.../platform/AndroidPlatformServices.kt`
- `iosPlatformServices(presenter)`: `shared/src/iosMain/.../platform/IosPlatformServices.kt`

## How a "Copy" tap reaches the OS

```mermaid
sequenceDiagram
    actor U as User
    participant S as TranslatorScreen (common)
    participant R as TranslatorRoute (common)
    participant CS as ClipboardService (interface)
    participant A as AndroidClipboardService
    participant I as IosClipboardService
    U->>S: taps Copy
    S->>R: onCopy(state.output)
    R->>CS: copyText(output)
    alt running on Android
        CS->>A: setPrimaryClip
    else running on iOS
        CS->>I: UIPasteboard.string = output
    end
```

The common code **never knows** which one runs. The platform was chosen once, at startup:

| Platform | Created in | Creates |
| --- | --- | --- |
| Android | `MorseKitApplication.container` (lazy, once per process) | `AppContainer(androidPlatformServices(this))` |
| iOS | `MainViewController()` (once per launch) | `AppContainer(iosPlatformServices(presenter = { controller }))` |

The container is passed into `App(container)` and handed down as parameters. There's no DI
framework, no global singleton, and no service locator.

## The app container

```kotlin
class AppContainer(val platformServices: PlatformServices) {
    val settingsRepository = SettingsRepository(platformServices.keyValueStore)
}
```

`AppContainer` holds objects that must be **shared by several screens** and **outlive a single
screen**. Settings are the first: `App` reads the theme, and the Settings screen changes it, so
both must observe the same `SettingsRepository`.

```mermaid
flowchart TB
    subgraph Process["App process"]
        MKA["MorseKitApplication<br/>(Android)"] --> C["AppContainer"]
        C --> PS["PlatformServices"]
        C --> SR["SettingsRepository"]
    end
    subgraph Activity["MainActivity (recreated on rotation)"]
        App["App(container)"]
    end
    App -->|"reads theme"| SR
    App --> Settings["SettingsRoute"] -->|"changes settings"| SR
```

> **Why not create it in `MainActivity`?** Android recreates the Activity on rotation. A
> repository created there would be replaced each time, while anything retained across rotation
> (like a ViewModel) would still hold the old one. `Application` lives as long as the process, so
> that's where app-scoped objects belong. iOS has no such recreation, so `MainViewController()`
> is enough.

## Interfaces vs `expect`/`actual`

KMP offers two ways to reach platform code. The template originally used `expect`/`actual`
(`expect fun getPlatform(): Platform`); we switched to interfaces for services.

| | `expect`/`actual` | Interface + implementations (used here) |
| --- | --- | --- |
| How | `expect fun foo()` in common, `actual fun foo()` in each platform source set | `interface Foo` in common, `class AndroidFoo : Foo` etc. |
| Construction parameters | Hard: every `actual` must match the same signature | Easy: Android takes a `Context`, iOS takes a view-controller provider |
| Fakes in tests | Not possible: there's exactly one `actual` per platform | Trivial: `InMemoryKeyValueStore()`, `clipboard = {}` |
| Best for | Small, parameterless platform facts or functions (e.g. current time, UUID, platform name) | Services with state, dependencies or side effects |
| Used in MorseKit | `KeepScreenOn()` and `rememberIsChangingConfigurations()` (`ui/platform/ScreenEffects.kt`, [note 9](09-flashlight.md)) | Everything in `PlatformServices` |

> **Concept: `fun interface`.** An interface with a single abstract method can be implemented with
> a lambda: `ClipboardService { text -> ... }`, or `{}` for a no-op. The Android `@Preview` in
> `MainActivity.kt` uses exactly that: `PlatformServices(clipboard = {}, share = {}, ...)`.

## Android details

```kotlin
fun androidPlatformServices(context: Context): PlatformServices {
    val appContext = context.applicationContext
    ...
}
```

| Detail | Why |
| --- | --- |
| Uses `applicationContext`, not the `Activity` | ViewModels outlive Activities (e.g. on rotation). Holding an `Activity` reference would leak it |
| `FLAG_ACTIVITY_NEW_TASK` on the share chooser | Required when starting an activity from a non-Activity context |
| No permissions needed | Clipboard and share intents don't need manifest permissions (vibration will need `VIBRATE`) |

## iOS details: Kotlin/Native interop

```kotlin
@OptIn(ExperimentalForeignApi::class)
override fun shareText(text: String) {
    val host = presenter()?.topmostPresented() ?: return
    val activity = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    activity.popoverPresentationController?.let { popover ->
        popover.sourceView = host.view
        popover.sourceRect = host.view.bounds.useContents {
            CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0)
        }
    }
    host.presentViewController(activity, animated = true, completion = null)
}
```

| Concept | Explanation |
| --- | --- |
| `platform.UIKit.*` imports | Kotlin/Native ships bindings for Apple frameworks, so UIKit classes are ordinary Kotlin classes |
| Obj-C categories become extensions | `popoverPresentationController` is declared in an Obj-C *category*, so in Kotlin it's an extension property you import |
| `CValue<CGRect>` and `useContents { }` | C structs (like `CGRect`) are passed by value as `CValue`. `useContents` gives temporary access to the fields |
| `@OptIn(ExperimentalForeignApi::class)` | C-interop APIs require opting in |
| Popover anchor | On iPad the share sheet is a popover and **crashes** without `sourceView`/`sourceRect` |
| `presenter: () -> UIViewController?` | The service needs the Compose view controller, which is created *after* the services, so we pass a lambda that reads it lazily |

The lazy presenter in `MainViewController.kt`:

```kotlin
fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    val container = AppContainer(iosPlatformServices(presenter = { controller }))
    controller = ComposeUIViewController { App(container) }
    return controller
}
```

The lambda reads `controller` only when the user taps Share, by which time it's been assigned.

## Audio

`PcmAudioPlayer` is the audio abstraction: it only plays rendered samples, and all Morse timing
lives in shared code. It's covered in [Audio playback](08-audio-playback.md).

## Flashlight

`TorchController` has just `isAvailable` and `setTorch(on)`: the platform only switches the LED,
and the timing loop is shared. See [Flashlight transmission](09-flashlight.md).

| | Android | iOS |
| --- | --- | --- |
| API | `CameraManager.setTorchMode(id, on)` (no permission needed) | `AVCaptureDevice.torchMode` inside `lockForConfiguration()` |
| Availability | A camera with `FLASH_INFO_AVAILABLE`, preferring the back one | `hasTorch && torchAvailable` |
| Failure | `CameraAccessException` → `false` | Checked up front (unsupported modes raise uncatchable Obj-C exceptions) |

## Vibration

`VibrationController` plays a complete shared `VibrationPattern` natively (Android waveform,
iOS Core Haptics) and can cancel it. See [Vibration transmission](10-vibration.md).

## All platform services at a glance

| Interface | Android | iOS | Permission |
| --- | --- | --- | --- |
| `ClipboardService` | `ClipboardManager` | `UIPasteboard` | none |
| `ShareService` | `Intent.ACTION_SEND` | `UIActivityViewController` | none |
| `KeyValueStore` | `SharedPreferences` | `NSUserDefaults` | none |
| `PcmAudioPlayer` | `AudioTrack` | `AVAudioEngine` | none |
| `TorchController` | `CameraManager.setTorchMode` | `AVCaptureDevice.torchMode` | none |
| `VibrationController` | `VibrationEffect.createWaveform` | Core Haptics | Android `VIBRATE` (normal) |
| `ReviewService` | Play In-App Review (`com.google.android.play:review`); `FakeReviewManager` in debuggable builds | StoreKit `SKStoreReviewController.requestReviewInScene` | none |
| `AppUpdateService` | Play In-App Updates (`com.google.android.play:app-update`); `FakeAppUpdateManager` in debuggable builds | None (`NoAppUpdateService`): MorseKit stays offline | none |

## Store ratings (`ReviewService`)

Both stores require their **native** rating prompt; custom "rate us" dialogs aren't allowed
(Google Play's in-app review policy; App Store Review Guideline 5.6.1). Both stores also decide
whether the prompt actually appears (a Play quota; on iOS at most 3 times a year), so a request
may show nothing. That's expected.

| Piece | Where | Notes |
| --- | --- | --- |
| When to ask | `core/review/ReviewPrompter` (tested) | At least 3 days since first use, at least 5 successful uses (copy, share, transmit), at most once per 120 days; counters in `KeyValueStore` |
| Asking moments | `TranslatorRoute` | After a copy, or when a transmission **finishes by itself** (not when you stop it, type or swap). Never at launch, never as the share sheet opens |
| Native prompt | `AndroidReviewService` / `IosReviewService` | Android needs an `Activity`: `MorseKitApplication` tracks the resumed one in a `WeakReference` |
| "Rate MorseKit" banner | Settings → About | An explicit tap opens the store page (allowed on both stores). No "do you like it?" question. Hidden on iOS until `StoreListing.APP_STORE_ID` exists |
| Store identifiers | `platform/StoreListing` | One place for the Play package and URL (also used by the share text) and the future App Store ID |

> **Testing before publishing.** Play's sheet only appears for apps installed from Google Play.
> Debuggable builds use `FakeReviewManager`, which runs the whole request → launch flow without
> showing UI or submitting anything. To see the real sheet, install through a Play internal
> testing track or internal app sharing.

## In-app updates (`AppUpdateService`)

| Platform | How |
| --- | --- |
| Android | **Google Play In-App Updates** (`com.google.android.play:app-update` 2.1.0). It talks to the Play Store app over IPC, so MorseKit still has **no internet permission** (checked in the merged manifest) |
| iOS | **None.** Apple has no in-app update API; the usual workaround (querying the iTunes lookup API) would be MorseKit's first network request, breaking the "doesn't use the network" promise. iOS's automatic App Store updates keep the app current |

**Modes (Android).** *Flexible* for normal releases: Play asks, downloads in the background, and
MorseKit shows **"Update ready: Restart / Later"** when it's done. *Immediate* (full-screen,
blocking) only when the update is urgent: `updatePriority` ≥ 4 (set when publishing through the Play
Developer API, e.g. fastlane or gradle-play-publisher; the Play Console UI can't set it) or ignored
for ≥ 30 days (`clientVersionStalenessDays`).

```mermaid
flowchart LR
    R["App resumes"] --> C{"store reports?"}
    C -->|"immediate update in progress"| I["resume Immediate"]
    C -->|"update, and a daily check is due"| M{"chooseUpdateMode"}
    M -->|urgent| I
    M -->|normal, not snoozed| F["Flexible: Play asks"]
    F -->|"Not now"| S["snooze this version 7 days"]
    F -->|"accept"| D["downloads in background"] --> RD["Update ready: Restart / Later"]
```

| Piece | Where | Tested by |
| --- | --- | --- |
| Mode choice (`chooseUpdateMode`, `AvailableUpdate.isUrgent`) | `core/update/AppUpdates.kt` | `ChooseUpdateModeTest` |
| Daily check limit, 7-day snooze (urgent and manual checks ignore it) | `core/update/UpdatePrompter.kt` | `UpdatePrompterTest` |
| Orchestration: check on resume, resume interrupted immediate updates, ready-to-install state, manual checks | `core/update/UpdateController.kt` (app-scoped, in `AppContainer`) | `UpdateControllerTest` (fake service) |
| Dialogs: "Update ready", "You're up to date" | `feature/update/UpdateDialogs.kt` | on device |
| "Check for updates" (Android only) | Settings → About | on device |

> **Testing before publishing.** Play's update flow only works for apps installed from Google
> Play. Debuggable builds use `FakeAppUpdateManager`: automatic checks report no update (so
> nothing pops up while developing), and **Settings → Check for updates** simulates a newer
> version, accepted and downloaded, which shows the real "Update ready" dialog. To test the real
> Play flow, use internal app sharing or a testing track with two builds of increasing
> `VERSION_CODE`.

