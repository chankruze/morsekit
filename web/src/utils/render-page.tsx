import { StrictMode, type ReactNode } from "react";
import { createRoot } from "react-dom/client";
import "@/styles/index.css";

const ROOT_ELEMENT_ID = "root";

export const renderPage = (page: ReactNode) => {
  const root = document.getElementById(ROOT_ELEMENT_ID);
  if (!root) throw new Error(`#${ROOT_ELEMENT_ID} is missing from the page`);
  createRoot(root).render(<StrictMode>{page}</StrictMode>);
};
