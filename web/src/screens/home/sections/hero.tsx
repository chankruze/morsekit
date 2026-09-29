import { Link } from "react-router";
import logo from "@/assets/logo.png";
import { ROUTES } from "@/constants/routes";
import { SECTION_IDS } from "@/constants/sections";
import { MorseSignal } from "@/screens/home/components/morse-signal";
import { PlayStoreButton } from "@/screens/home/components/play-store-button";

export const HeroSection = () => (
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
      Translate, learn and send Morse code as sound, light or vibration. No
      signal needed.
    </p>
    <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
      <PlayStoreButton />
      <Link
        to={{ pathname: ROUTES.home, hash: SECTION_IDS.tryIt }}
        className="rounded-2xl px-5 py-4 font-medium text-cream/85 transition hover:bg-white/10"
      >
        Try it here ↓
      </Link>
    </div>
    <div className="mt-16 w-full max-w-2xl">
      <MorseSignal />
    </div>
  </section>
);
