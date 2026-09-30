import { EFFECTIVE_DATE } from "@/screens/privacy/constants/policy";

export const PolicyHeaderSection = () => (
  <>
    <h1 className="text-4xl font-bold sm:text-5xl">Privacy policy</h1>
    <p className="mt-3 text-cream/60">MorseKit · Effective {EFFECTIVE_DATE}</p>
    <div className="mt-8 rounded-3xl bg-brand/30 p-6 text-lg">
      <p>
        <strong>In short:</strong> MorseKit doesn't collect, send, share or sell
        any personal data. It has no internet permission, no account, no ads and
        no analytics. Everything you type and every setting stays on your
        device. The only exception is optional: if you join the beta test, the
        developer sees your email address (see Beta testing).
      </p>
    </div>
  </>
);
