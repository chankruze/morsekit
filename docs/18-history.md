# 18. History

The translator keeps the translations you **use** (copy, share, play, flash or vibrate), so
you can find them again. The **history** icon in the translator's top bar opens the History
screen; tapping an entry puts it back in the translator.

```
┌──────────────────────────┐
│ ← History              ⋮ │  ⋮ → Clear history (keeps favourites)
│ [   All   | Favourites ] │
│ ┌──────────────────────┐ │
│ │ Text → Morse     ★ 🗑 │ │  star = favourite; delete has Undo
│ │ SOS                  │ │
│ │ ••• −−− •••          │ │
│ └──────────────────────┘ │
└──────────────────────────┘
```

## What's saved, and when

| Rule | Why |
| --- | --- |
| Saved only when a translation is **used**: Copy, Share, or a transmission starting | Nothing half-typed; history is what you actually sent |
| Using the same text again (same direction, same trimmed input) moves it to the top | No duplicates |
| The newest **50** non-favourites are kept; **favourites** are never dropped | Bounded storage, but starred messages are safe |
| **Clear history** removes everything except favourites; a favourite is removed with Delete | A clear-all can't wipe what you chose to keep |
| **Save history** (Settings › History, on by default) turns saving off; existing entries stay until cleared | Your choice; Reset to defaults doesn't turn it back on |
| The **☆** in the translator's output card saves the translation as a favourite (★), even with Save history off; tapping ★ un-stars it (it stays in History) | An explicit "keep this", rather than a second way to do what saving-on-use already does |

The engine is `core/history`: `HistoryRepository` (a `StateFlow` of entries, newest first) and
`HistoryCodec`.

### Storing it without a JSON library

There's no serialization format in the build (and new dependencies can't be downloaded
offline), so `HistoryCodec` writes one string: entries separated by ASCII **record separator**
(`\u001E`), fields by **unit separator** (`\u001F`). Free text escapes `\`, `\u001E` and
`\u001F`, so any message (newlines, emoji, quotes, even those two characters) survives. A
version prefix (`h1`) allows a future format; entries that don't parse are skipped, so corrupt
data loses those entries, not the app. A test with the escaping removed fails, which is how the
test was checked.

## Never backed up

Settings and practice progress can travel in the user's device backup; typed messages
shouldn't. So History has its **own store** (`PlatformServices.historyStore`):

| Platform | Store | Kept out of backups by |
| --- | --- | --- |
| Android | SharedPreferences file `morsekit_history` | `res/xml/data_extraction_rules.xml` (Android 12+: cloud backup **and** device transfer) and `res/xml/backup_rules.xml` (Android 11 and below), both excluding `morsekit_history.xml` |
| iOS | One UTF-8 file per key in Application Support/History | `NSURLIsExcludedFromBackupKey` on the folder and on every file after each write (`NSUserDefaults` can't be excluded) |

The iOS store compiles against the real Foundation APIs but hasn't run on a device yet (no
Xcode on the build machine).

**Checking it on Android** (debug build, about two minutes): change a setting you'll recognise
(e.g. Dark theme) and make some history, then

```bash
adb shell bmgr enabled                                # "Backup Manager currently enabled"
adb shell bmgr backupnow in.geekofia.morsekit.debug   # back the app up now
adb shell pm clear in.geekofia.morsekit.debug         # wipe its data, as on a new phone
adb shell bmgr restore in.geekofia.morsekit.debug     # restore from that backup
```

Open MorseKit: the theme should be back (the backup worked) and History empty (it was
excluded). If backupnow reports backup is disabled, turn on the phone's backup (System ›
Backup) first.

## Opening an entry in the translator

History and the translator are separate screens with their own view models. History leaves the
entry in `TranslationRequests` (app-scoped, in `AppContainer`) and goes back; the translator
collects it, stops any transmission, sets the direction and input, and consumes the request.

## Navigation: the first detail screen

History is a **detail screen** on top of the Translator tab, not a sixth tab (five is the
bottom bar's maximum). See [note 12](12-navigation.md) for how the back stack holds it.

## Privacy

History changes what the app keeps, so the privacy policy and the About text say so: History is
on by default, saved only on use, can be turned off or cleared, and is never backed up. The
policy's guard test failed until the `history` data was described ([note 14](14-landing-page.md)).

## Tests

| Test | Covers |
| --- | --- |
| `HistoryRepositoryTest` (12) | Newest first, blank not saved, re-use moves to the top and keeps the favourite, directions kept apart, the 50 limit with favourites kept, delete and clear, restore (Undo) in place, awkward text across a restart, corrupt data, the codec round trip, starring (no duplicates; un-starring keeps the entry), find |
| `AppBackStackDetailTest` (6) | A detail screen on top of its tab, back closes it first, kept while another tab is selected, re-selecting the tab closes it, save and restore, unknown saved names, detail content keys |
| `SettingsRepositoryTest` | Save history's default and persistence; Reset keeps it |
