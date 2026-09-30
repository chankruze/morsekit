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
- Reference cells read as one sentence: "A, dot dash", "Comma, dash dash …", "Prosign S O S,
  Distress signal, dot dot dot, …". Prosign letters are spaced so they're spelled, not read as
  a word.

## Rules the UI follows

| Rule | Where |
| --- | --- |
| Every icon button has a label ("Copy", "Share", "Clear", "Search", "More options"); decorative icons (the logo next to "MorseKit", the star in the rating banner, nav-bar icons next to their labels) have `contentDescription = null` | All screens |
| Screen titles, card titles, Settings sections and Reference sections are **headings** (`semantics { heading() }`), so screen-reader users can jump between them | `ScreenScaffold` callers, `SectionCard`, `CardHeader`, `ReferenceGrid` |
| Controls announce their value in words: speed "Speed 20 words per minute" (also a polite **live region**, so − / + announce the new value), sliders "20 WPM" / "600 Hz" | `SpeedStepper`, `SliderSetting` |
| One stop per action: each Transmit option is read once, as its button, with its note ("Flash, No flashlight"); the label chip beside it is hidden from screen readers (`clearAndSetSemantics {}`) because the button does the same thing | `OptionRow` |
| Unavailable options are marked `disabled()` and say why | `OptionRow` |
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
