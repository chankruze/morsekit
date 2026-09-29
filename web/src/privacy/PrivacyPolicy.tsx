import type { ReactNode } from "react";
import { GITHUB_URL, ORGANIZATION_URL } from "../links";
import { EFFECTIVE_DATE, PERMISSIONS, STORED_DATA } from "./policy";

const GOOGLE_PRIVACY = "https://policies.google.com/privacy";
const GITHUB_PRIVACY =
  "https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement";
const CONTACT = `${GITHUB_URL}/issues`;
const HISTORY = `${GITHUB_URL}/commits/main/web/src/privacy`;

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="mt-10">
      <h2 className="text-2xl font-semibold">{title}</h2>
      <div className="mt-3 space-y-3 text-cream/80">{children}</div>
    </section>
  );
}

function Link({ href, children }: { href: string; children: ReactNode }) {
  return (
    <a href={href} className="text-cyan underline-offset-2 hover:underline">
      {children}
    </a>
  );
}

export function PrivacyPolicy() {
  return (
    <article className="mx-auto max-w-3xl px-5 py-16">
      <h1 className="text-4xl font-bold sm:text-5xl">Privacy policy</h1>
      <p className="mt-3 text-cream/60">
        MorseKit · Effective {EFFECTIVE_DATE}
      </p>

      <div className="mt-8 rounded-3xl bg-brand/30 p-6 text-lg">
        <p>
          <strong>In short:</strong> MorseKit doesn't collect, send, share or
          sell any personal data. It has no internet permission, no account, no
          ads and no analytics. Everything you type and every setting stays on
          your device.
        </p>
      </div>

      <Section title="Who we are">
        <p>
          MorseKit is an app for translating and sending Morse code, developed
          by chankruze at <Link href={ORGANIZATION_URL}>geekofia</Link>. This
          policy covers the MorseKit app and this website.
        </p>
      </Section>

      <Section title="What MorseKit doesn't collect">
        <p>
          MorseKit doesn't collect personal information of any kind. There are
          no accounts, no analytics or crash-reporting tools, no advertising,
          and no tracking. The app can't reach the internet: it doesn't request
          Android's internet permission, so it has no way to send anything
          anywhere.
        </p>
        <p>
          It doesn't access your location, contacts, photos, camera or
          microphone. Flashing the flashlight uses Android's torch control,
          which doesn't need camera access, and nothing is ever recorded.
        </p>
      </Section>

      <Section title="What stays on your device">
        <p>To remember your preferences, MorseKit stores a few values on your device only:</p>
        <ul className="list-disc space-y-2 pl-6">
          {STORED_DATA.map((d) => (
            <li key={d.prefix}>{d.what}</li>
          ))}
        </ul>
        <p>
          What you type into the translator isn't kept as a history and is
          never sent anywhere. None of this data identifies you, and it's
          deleted when you clear MorseKit's storage or uninstall the app.
        </p>
        <p>
          If you've turned on your device's backup (for example, Android backup
          to your Google account), your device may include these settings in
          that backup so they come back on a new phone. You control this in
          your device settings; the backup belongs to your account and the
          developer can't access it.
        </p>
      </Section>

      <Section title="Permissions">
        <ul className="space-y-2">
          {PERMISSIONS.map((p) => (
            <li key={p.name}>
              <strong className="text-cream">{p.label}</strong>{" "}
              <code className="text-sm text-cream/60">{p.name}</code>: {p.why}
            </li>
          ))}
        </ul>
        <p>MorseKit uses no other permissions.</p>
      </Section>

      <Section title="Copying and sharing">
        <p>
          When you tap Copy, the translation goes to your device's clipboard.
          When you tap Share, your device's share sheet opens and you choose
          where the message goes. MorseKit only does this when you ask, and the
          app you share with handles the message under its own privacy policy.
        </p>
      </Section>

      <Section title="Google Play features">
        <p>
          On Android, MorseKit uses two features of the Google Play Store app:
          in-app updates (to tell you a new version is available) and in-app
          reviews (to let you rate MorseKit without leaving it). Both are
          provided by Google Play, which handles them under the{" "}
          <Link href={GOOGLE_PRIVACY}>Google Privacy Policy</Link>. MorseKit
          only learns whether an update is available; it never learns whether
          you left a review or what it said.
        </p>
        <p>
          If you've allowed your device to share usage and diagnostics data
          with Google, Google Play may give the developer anonymous, aggregated
          statistics such as crash rates. These can't identify you.
        </p>
      </Section>

      <Section title="Children">
        <p>
          MorseKit collects no personal information from anyone, including
          children.
        </p>
      </Section>

      <Section title="This website">
        <p>
          This website uses no cookies, no analytics and no third-party scripts
          or fonts. It's hosted on GitHub Pages, and GitHub may log technical
          data such as IP addresses to keep its service secure, under the{" "}
          <Link href={GITHUB_PRIVACY}>GitHub General Privacy Statement</Link>.
        </p>
      </Section>

      <Section title="Changes to this policy">
        <p>
          If this policy changes, the new version will be posted on this page
          with a new effective date. Every past version is public in the{" "}
          <Link href={HISTORY}>project's history on GitHub</Link>.
        </p>
      </Section>

      <Section title="Contact">
        <p>
          Questions about privacy? Open an issue at{" "}
          <Link href={CONTACT}>github.com/chankruze/morsekit/issues</Link>.
        </p>
      </Section>
    </article>
  );
}
