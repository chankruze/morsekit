import type { ReactNode } from "react";

type TextLinkProps = {
  href: string;
  children: ReactNode;
};

export const TextLink = ({ href, children }: TextLinkProps) => (
  <a href={href} className="text-cyan underline-offset-2 hover:underline">
    {children}
  </a>
);
