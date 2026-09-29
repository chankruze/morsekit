import logo from "./assets/logo.png";
import { PLAY_PUBLISHED, PLAY_URL } from "./links";
import { Icon, type IconName } from "./components/icons";
import {
  PRIVACY_POLICY,
  SiteFooter,
  SiteHeader,
} from "./components/SiteChrome";
import { MorseSignal } from "./components/MorseSignal";
import { Translator } from "./components/Translator";
import referenceLight from "./assets/screenshots/reference-light.webp";
import settingsDark from "./assets/screenshots/settings-dark.webp";
import translatorLight from "./assets/screenshots/translator-light.webp";
import transmitDark from "./assets/screenshots/translator-transmit-dark.webp";

function PlayButton() {
  const content = (
    <>
      <svg viewBox="0 0 24 24" aria-hidden="true" className="size-6">
        <path
          fill="#07c7fe"
          d="M3.6 1.8 13.4 12l-9.8 10.2c-.4-.2-.6-.6-.6-1.1V2.9c0-.5.2-.9.6-1.1Z"
        />
        <path
          fill="#fdc31a"
          d="m16.6 8.7-3.2 3.3 3.2 3.3 3.7-2.1c.9-.5.9-1.9 0-2.4l-3.7-2.1Z"
        />
        <path
          fill="#fd5a2c"
          d="M13.4 12 3.6 22.2c.3.2.8.2 1.2 0l11.8-6.9-3.2-3.3Z"
        />
        <path
          fill="#fdfaf0"
          d="M3.6 1.8c.3-.2.8-.2 1.2 0l11.8 6.9-3.2 3.3-9.8-10.2Z"
        />
      </svg>
      <span className="text-left leading-tight">
        <span className="block text-xs text-cream/70">
          {PLAY_PUBLISHED ? "Get it on" : "Coming soon to"}
        </span>
        <span className="block text-lg font-semibold">Google Play</span>
      </span>
    </>
  );
  const style =
    "flex items-center gap-3 rounded-2xl border border-white/15 bg-black/40 px-5 py-2.5";
  return PLAY_PUBLISHED ? (
    <a href={PLAY_URL} className={`${style} transition hover:bg-black/60`}>
      {content}
    </a>
  ) : (
    <div className={`${style} opacity-80`}>{content}</div>
  );
}

const FEATURES: { icon: IconName; title: string; text: string }[] = [
  {
    icon: "translate",
    title: "Live translator",
    text: "Type text or Morse and see the other side as you type. Swap, copy and share in a tap.",
  },
  {
    icon: "volume",
    title: "Hear it",
    text: "Clean, click-free tones from 5 to 60 words per minute, at the pitch you like.",
  },
  {
    icon: "flashlight",
    title: "Flash it",
    text: "Signal with the flashlight, timed precisely, with a warning before the first flash.",
  },
  {
    icon: "vibration",
    title: "Feel it",
    text: "Send Morse as vibration you can read by touch.",
  },
  {
    icon: "reference",
    title: "Reference chart",
    text: "Every letter, number and punctuation mark, searchable.",
  },
  {
    icon: "settings",
    title: "Your way",
    text: "Light or dark theme, your default speed and tone.",
  },
];

const SCREENS = [
  {
    src: transmitDark,
    alt: "Translator in dark theme, with the Transmit menu open: speed, Sound, Flash and Vibrate",
  },
  {
    src: translatorLight,
    alt: "Translator in light theme, turning the letter d into dash dot dot",
  },
  {
    src: referenceLight,
    alt: "Reference chart of letters with their Morse codes",
  },
  {
    src: settingsDark,
    alt: "Settings in dark theme: theme, playback speed and tone",
  },
];

const PRIVACY = [
  {
    title: "No internet permission",
    text: "MorseKit can't go online, so your messages never leave your phone.",
  },
  {
    title: "No ads, no tracking",
    text: "No analytics, no account, nothing to sign up for.",
  },
  {
    title: "One permission",
    text: "Vibration, used only to send Morse by vibration.",
  },
];

export const App = () => {
  return (
    <div className="relative overflow-x-clip">
      {/* Glow behind the hero, in the logo's colours. */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-40 left-1/2 h-[42rem] w-[72rem] -translate-x-1/2 rounded-full bg-[radial-gradient(closest-side,rgba(2,75,198,0.55),rgba(7,199,254,0.12),transparent)]"
      />

      <SiteHeader onHome />

      <main id="top">
        <section className="relative mx-auto flex max-w-6xl flex-col items-center px-5 pt-20 pb-16 text-center sm:pt-28">
          <img
            src={logo}
            alt="MorseKit logo"
            className="size-24 rounded-3xl shadow-2xl shadow-brand/50 sm:size-28"
          />
          <p className="mt-8 text-sm font-medium tracking-widest text-cyan uppercase">
            Free · Android · Works offline
          </p>
          <h1 className="mt-4 max-w-3xl text-5xl font-bold tracking-tight text-balance sm:text-7xl">
            Morse code, in your pocket.
          </h1>
          <p className="mt-6 max-w-xl text-lg text-balance text-cream/75">
            Translate, learn and send Morse code as sound, light or vibration.
            No signal needed.
          </p>
          <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
            <PlayButton />
            <a
              href="#try"
              className="rounded-2xl px-5 py-4 font-medium text-cream/85 transition hover:bg-white/10"
            >
              Try it here ↓
            </a>
          </div>
          <div className="mt-16 w-full max-w-2xl">
            <MorseSignal />
          </div>
        </section>

        <section
          aria-label="Screenshots"
          className="mx-auto max-w-6xl px-5 py-12"
        >
          <ul className="-mx-5 flex snap-x snap-mandatory gap-5 overflow-x-auto px-5 pb-4 lg:mx-0 lg:grid lg:grid-cols-4 lg:overflow-visible lg:px-0">
            {SCREENS.map((s, i) => (
              <li
                key={s.src}
                className={`w-60 shrink-0 snap-center lg:w-auto ${i % 2 ? "lg:translate-y-8" : ""}`}
              >
                <img
                  src={s.src}
                  alt={s.alt}
                  width={540}
                  height={1157}
                  loading="lazy"
                  className="w-full rounded-[2rem] border-4 border-white/10 shadow-2xl shadow-black/50"
                />
              </li>
            ))}
          </ul>
        </section>

        <section id="try" className="mx-auto max-w-3xl scroll-mt-20 px-5 py-16">
          <h2 className="text-center text-3xl font-bold sm:text-4xl">Try it</h2>
          <p className="mt-3 mb-8 text-center text-cream/70">
            The same alphabet and timing as the app, right in your browser.
          </p>
          <Translator />
        </section>

        <section
          id="features"
          className="mx-auto max-w-6xl scroll-mt-20 px-5 py-16"
        >
          <h2 className="text-center text-3xl font-bold sm:text-4xl">
            Everything for Morse
          </h2>
          <ul className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map((f) => (
              <li
                key={f.title}
                className="rounded-3xl border border-white/10 bg-white/[0.03] p-6"
              >
                <span className="inline-flex rounded-2xl bg-brand/40 p-3 text-cyan">
                  <Icon name={f.icon} className="size-6" />
                </span>
                <h3 className="mt-4 text-xl font-semibold">{f.title}</h3>
                <p className="mt-2 text-cream/70">{f.text}</p>
              </li>
            ))}
          </ul>
        </section>

        <section
          id="privacy"
          className="mx-auto max-w-6xl scroll-mt-20 px-5 py-16"
        >
          <div className="rounded-[2rem] bg-gradient-to-br from-brand to-navy p-8 sm:p-12">
            <h2 className="max-w-xl text-3xl font-bold sm:text-4xl">
              Works offline. Stays private.
            </h2>
            <ul className="mt-8 grid gap-6 sm:grid-cols-3">
              {PRIVACY.map((p) => (
                <li key={p.title}>
                  <h3 className="flex items-center gap-2 text-lg font-semibold">
                    <span
                      className="size-2.5 rounded-full bg-sun"
                      aria-hidden="true"
                    />
                    {p.title}
                  </h3>
                  <p className="mt-2 text-cream/80">{p.text}</p>
                </li>
              ))}
            </ul>
            <a
              href={PRIVACY_POLICY}
              className="mt-8 inline-block font-medium text-cyan hover:underline"
            >
              Read the privacy policy →
            </a>
          </div>
        </section>
      </main>

      <SiteFooter />
    </div>
  );
};
