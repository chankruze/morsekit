import type { ReactNode } from "react";

type PolicySectionProps = {
  title: string;
  children: ReactNode;
};

export const PolicySection = ({ title, children }: PolicySectionProps) => (
  <section className="mt-10">
    <h2 className="text-2xl font-semibold">{title}</h2>
    <div className="mt-3 space-y-3 text-cream/80">{children}</div>
  </section>
);
