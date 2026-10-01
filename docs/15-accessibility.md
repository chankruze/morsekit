# 15. Accessibility

MorseKit is mostly symbols (`•`, `−`) and icons, which screen readers handle badly by default.
This note lists what each part of the UI does so TalkBack (Android) and VoiceOver (iOS) users
get the same app, and how to check it.

## Morse is spoken as dots and dashes

The display glyphs `•` and `−` would be read as "bullet" and "minus". `MorseNotation.toSpokenForm`
turns canonical Morse into speech, and every place that shows Morse uses it:

| Shown | Spoken |
| --- | --- |
| `••• −−− •••` | "dot dot dot, dash dash dash, dot dot dot" (commas give a pause between letters) |
| `•− / −•••` | "dot dash, space, dash dot dot dot" |

- `MorseDisplay` (the translator's output) sets it as the text's `contentDescription`; the text
  stays selectable.
- History reads a Morse input the same way (its text is raw `.-`, which would otherwise be read
  as punctuation or skipped).
- Reference cells read as one sentence: "A, dot dash", "Comma, dash dash …", "Prosign S O S,
  Distress signal, dot dot dot, …". Prosign letters are spaced so they're spelled, not read as
  a word.

## Rules the UI follows

| Rule | Where |
| --- | --- |
| Every icon button has a label ("Copy", "Share", "Clear", "Search", "More options"); decorative icons (the logo next to "MorseKit", the star in the rating banner, nav-bar icons next to their labels) have `contentDescription = null` | All screens |
| Screen titles, card titles, Settings sections and Reference sections are **headings** (`semantics { heading() }`), so screen-reader users can jump between them | `ScreenScaffold` callers, `SectionCard`, `CardHeader`, `ReferenceGrid` |
| Controls announce their value in words: speed "Speed 20 words per minute" (also a polite **live region**, so − / + announce the new value), sliders "Speed, 20 WPM" / "Tone, 600 Hz" (named, value as `stateDescription`) | `SpeedStepper`, `SliderSetting` |
| One stop per action: each Transmit option is read once, as its button, with its note ("Flash, No flashlight"); the label chip beside it is hidden from screen readers (`clearAndSetSemantics {}`) because the button does the same thing | `OptionRow` |
| Unavailable options are marked `disabled()` and say why | `OptionRow` |
| Symbols are spoken as words: the Dot / Dash buttons hide their `•` / `−` (the label says it), Learn's answer marks are "M, right answer" / "R, your answer" (not "check mark" / "ballot X"), History's "Morse → Text" is "Morse to Text" | `TapButtons`, `Choices`, `HistoryItem` |
| Text fields keep a name once typed in: a placeholder ("Enter text") goes away, so the borderless translator field announces its card title ("Text" / "Morse") and the Reference search field "Search" | `MorseTextField`, `HeaderSearchField` |
| A switch with a label is one row (`toggleable` on the row, the `Switch` itself `onCheckedChange = null`), so it's read as "Save history, …, On" and the whole row is the target | Settings › History |
| The open Transmit menu works like a dialog: its scrim is a "Close transmit options" control covering the screen, so the translator underneath (which can't be used until the menu closes) is hidden from screen readers. Compose leaves out nodes that a later, labelled node covers completely | `TransmitFab` |
| Icon-and-label actions (Learn's *Play again* and *Show Morse*) are one touch target each (`clickable` / `toggleable` on the pair), so the label is read once, with its role (button, switch) and state |
| Touch targets are at least 48 dp: Material buttons, icon buttons and clickable `Surface`s enforce it; the privacy policy link is a `TextButton`, not a small inline link | Settings › About |
| Text scales with the system font size: sizes are in `sp` through the Material type scale, and containers use minimum heights (`heightIn(min = 48.dp)`), not fixed ones, so large fonts grow them instead of clipping | `CardHeader` |
| Motion follows the user: the landing page's blinking signal stops with "Reduce motion" ([note 14](14-landing-page.md)); the flashlight warns before its first flash ([note 9](09-flashlight.md)) | |

## How to check

| Check | How |
| --- | --- |
| Screen reader | Android: Settings › Accessibility › TalkBack. Swipe through each screen; the reading order should follow the layout, and nothing should say "bullet" or "unlabelled" |
| Headings | TalkBack's reading controls › Headings, then swipe up/down to jump between sections |
| Large fonts | `adb shell settings put system font_scale 2.0` (reset with `1.0`), or Settings › Display › Font size. Look for clipped or overlapping text |
| Touch targets and contrast | Google's **Accessibility Scanner** app; Play Console's pre-launch report runs similar checks on every upload |
| What TalkBack gets, without turning it on | `adb shell uiautomator dump /sdcard/a11y.xml`, then `adb pull` it: every node with its `text`, `content-desc`, state (`checked`, `selected`, `enabled`) and bounds. Look for tappable nodes with no text or description in them, and bounds under 48 dp (at 480 dpi that's 144 px). A node cut off by the screen edge looks small; scroll and dump again. Sliders are `focusable`, not `clickable`, and a merged button's label is on a child node |

## Device check, 1 October 2026

Checked the accessibility tree over adb (as above) on a OnePlus phone (Android 16, build 1.1.1), on every screen and state:
the translator empty, typed and with Transmit open, Tap in both modes, Learn (Listen, Key, a wrong answer), Reference and
its search, Settings, History. Found and fixed:

| Found | Fix |
| --- | --- |
| With Transmit open, the translator behind the scrim was still reachable, and Share (half covered by the speed control) had no label | The scrim is a close control (above) |
| The Save history switch had no label: "On, switch" | The row is the switch |
| The Speed and Tone sliders had a value but no name | `contentDescription` |
| Dot / Dash read their symbols too ("bullet, Dot") | Symbol hidden |
| Learn's ✓ / ✗ were read as symbols | Spoken as "right answer" / "your answer" |
| History read a Morse input as punctuation, and "→" as an arrow | Spoken form; "to" |
| The translator and search fields lost their name once typed in | `contentDescription` |

Everything else was already right: labels on every icon button, disabled states when there's no output, the selected tab,
headings, Learn's feedback as a live region ("It was U, dot dot dash"), the Morse key's actions (double-tap for a dot,
Dash / End letter / Space in TalkBack's actions menu), Reference cells, the privacy policy button. This checked what TalkBack
is given; it wasn't listened to with TalkBack on, so the exact wording and pauses are TalkBack's own.
