import type { ReactNode } from "react";

type PrivacyLayoutProps = {
  children: ReactNode;
};

export const PrivacyLayout = ({ children }: PrivacyLayoutProps) => (
  <article className="mx-auto max-w-3xl px-5 py-16">{children}</article>
);
