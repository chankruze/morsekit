import type { ReactNode } from "react";

type SectionHeadingProps = {
  children: ReactNode;
  className?: string;
};

export const SectionHeading = ({
  children,
  className = "",
}: SectionHeadingProps) => (
  <h2 className={`text-3xl font-bold sm:text-4xl ${className}`}>{children}</h2>
);
