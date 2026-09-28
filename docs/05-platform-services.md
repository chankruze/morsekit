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
    class PlatformServices {
        +clipboard: ClipboardService
        +share: ShareService
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
    ClipboardService <|.. AndroidClipboardService
    ShareService <|.. AndroidShareService
    ClipboardService <|.. IosClipboardService
    ShareService <|.. IosShareService
```

| Interface (`commonMain`) | Android (`androidMain`) | iOS (`iosMain`) |
| --- | --- | --- |
| `ClipboardService.copyText` | `ClipboardManager.setPrimaryClip(ClipData.newPlainText(...))` | `UIPasteboard.generalPasteboard.string = text` |
| `ShareService.shareText` | `Intent.ACTION_SEND` in `Intent.createChooser(...)` | `UIActivityViewController` presented from the top-most view controller |

Factory functions build the bundle for each platform:

- `AndroidPlatformServices(context)`: `shared/src/androidMain/.../platform/AndroidPlatformServices.kt`
- `IosPlatformServices(presenter)`: `shared/src/iosMain/.../platform/IosPlatformServices.kt`

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

| Platform | Entry point | Creates |
| --- | --- | --- |
| Android | `MainActivity.onCreate` | `AndroidPlatformServices(this)` |
| iOS | `MainViewController()` | `IosPlatformServices(presenter = { controller })` |

Both pass the result into `App(platformServices)`, and it's handed down as a parameter. There's
no DI framework, no global singleton, and no service locator.

## Interfaces vs `expect`/`actual`

KMP offers two ways to reach platform code. The template originally used `expect`/`actual`
(`expect fun getPlatform(): Platform`); we switched to interfaces for services.

| | `expect`/`actual` | Interface + implementations (used here) |
| --- | --- | --- |
| How | `expect fun foo()` in common, `actual fun foo()` in each platform source set | `interface Foo` in common, `class AndroidFoo : Foo` etc. |
| Construction parameters | Hard: every `actual` must match the same signature | Easy: Android takes a `Context`, iOS takes a view-controller provider |
| Fakes in tests | Not possible: there's exactly one `actual` per platform | Trivial: `PlatformServices(clipboard = {}, share = {})` |
| Best for | Small, parameterless platform facts or functions (e.g. current time, UUID, platform name) | Services with state, dependencies or side effects |

> **Concept: `fun interface`.** An interface with a single abstract method can be implemented with
> a lambda: `ClipboardService { text -> ... }`, or `{}` for a no-op. The Android `@Preview` in
> `MainActivity.kt` uses exactly that: `App(PlatformServices(clipboard = {}, share = {}))`.

## Android details

```kotlin
fun AndroidPlatformServices(context: Context): PlatformServices {
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
    val platformServices = IosPlatformServices(presenter = { controller })
    controller = ComposeUIViewController { App(platformServices) }
    return controller
}
```

The lambda reads `controller` only when the user taps Share, by which time it's been assigned.

## Planned services (not built yet)

These will follow the same pattern when playback is implemented:

| Interface | Android | iOS | Consumes |
| --- | --- | --- | --- |
| `TorchController` | `CameraManager.setTorchMode` | `AVCaptureDevice.torchMode` | `List<MorseSignal>` |
| `HapticController` | `Vibrator` / `VibrationEffect` (+ `VIBRATE` permission) | Core Haptics (`CHHapticEngine`) | `List<MorseSignal>` |
| `MorseAudioPlayer` | `AudioTrack` sine tone | `AVAudioEngine` | `List<MorseSignal>` |
