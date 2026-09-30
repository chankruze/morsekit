# 17. The Morse trainer (Learn)

The **Learn** tab teaches Morse by ear with the **Koch method**: characters are heard at full
speed from the start (so you learn each one's rhythm, not by counting dots), beginning with just
two and adding one at a time as you get them right. This is phase 1: **character mode** (hear a
character, pick it from four), **scoring** and **progression**. Reverse mode (see a character,
key it with the Tap decoder) and configurable sessions come next.

```
┌──────────────────────────┐
│ Learn                  ⋮ │  ⋮ → Reset progress
│ ┌──────────────────────┐ │
│ │ Level 5 of 41        │ │
│ │ K  M  U  R  E        │ │  unlocked; the newest in bold
│ │ ▓▓▓▓▓▓▓▓▓▓░░░░       │ │  right answers toward the next unlock
│ └──────────────────────┘ │
│ ┌──────────────────────┐ │
│ │      •−•             │ │  the Morse (hidden with "Show Morse" off)
│ │   [▶ Play again]     │ │  plays by itself for each question
│ │   Show Morse  ●      │ │
│ └──────────────────────┘ │
│  [ K ]      [ R ]        │  four choices
│  [ E ]      [ U ]        │
│  This session: 12 of 14 right · streak 5
└──────────────────────────┘
```

## The engine (`core/trainer`)

All pure Kotlin, tested without a UI.

| Piece | What it does |
| --- | --- |
| `KochOrder` | The 41-character order used by LCWO (lcwo.net): `K M U R E S N A P T L W I . J Z = F O Y , V G 5 / Q 9 2 H 3 8 B ? 4 7 C 1 D 6 0 X`. Learners start with K and M |
| `TrainerProgress` | The level (how many of the order are unlocked), per-character right/wrong counts, and the last answers |
| `KochProgression` | The unlock rule: **18 of the last 20** answers right (90%) unlocks the next character, and the window starts over so the new one is part of what's measured |
| `QuestionGenerator` | Picks the next character, weighted: `1 + 3 × error rate`, plus 2 for the newest character; never the same one twice in a row once three are unlocked. Up to four shuffled choices from the unlocked set. The random source is injected, so tests are repeatable |
| `TrainerRepository` | Saves progress under `trainer.*` keys. Missing, corrupt or out-of-range values fall back to a fresh start, never a crash |

> **The order isn't typed from the alphabet.** An earlier draft of the order had 40 characters;
> the test that checks for 41 unique characters, all 26 letters and all 10 digits caught it. The
> order was written from memory (lcwo.net isn't reachable from the build machine), so it's worth
> comparing with LCWO's once.

### Why these rules

| Choice | Reason |
| --- | --- |
| Full speed from the first character | The Koch method: slow characters teach counting, which has to be unlearned. Questions play at the playback speed from Settings (20 WPM by default) |
| 90% of the last 20 | High enough that a character is really known, short enough to move on in a few minutes |
| Missed characters weigh more | Practice goes where it's needed, not evenly |
| Four choices, from unlocked characters only | Fast on a phone; the other choices are characters you're learning, so telling them apart is the practice |

## The screen (`feature/trainer`)

- Each question **plays by itself**; *Play again* replays it. *Show Morse* can be turned off for
  ear-only practice (saved as `trainer.hideMorse`).
- A **right** answer moves on after 0.7 s. A **wrong** one marks the right choice (✓) and your
  pick (✗), shows the right character's Morse, and waits for *Next*.
- A new character is **introduced** in a dialog (with its Morse and a Play button) before the
  next question.
- The session score (right, answered, streak) is for this visit; per-character stats and the
  level are saved.
- *Reset progress* (⋮) goes back to K and M after a confirmation.

`TrainerViewModel` holds no coroutines: the 0.7 s pause and the automatic playback are
`LaunchedEffect`s in `TrainerRoute`, so the view model is tested directly.

## Privacy

The trainer's data (`trainer.*`) is a new kind of stored data, so the privacy policy's guard test
failed until the policy described it ([note 14](14-landing-page.md)), which it now does.

## Tests

| Test | Covers |
| --- | --- |
| `KochProgressionTest` (7) | The order (41 unique, all letters and digits, K and M first), per-character counts, 18/20 unlocks and 17/20 doesn't, only the last 20 count, nothing after the last character, invalid progress |
| `QuestionGeneratorTest` (6) | Choices valid and distinct, two choices at the start, no immediate repeats, the newest and missed characters come up more, same seed same questions |
| `TrainerRepositoryTest` (4) | Fresh start, round trip (including `,` `/` `.`), corrupt values, reset keeps the Show Morse option |
| `TrainerViewModelTest` (8) | Start, scoring and saving, first answer only, streaks, unlock and introduction, next question, survives restart, reset |
