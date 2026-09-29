import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "../index.css";
import { SiteFooter, SiteHeader } from "../components/SiteChrome";
import { PrivacyPolicy } from "./PrivacyPolicy";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <SiteHeader />
    <main>
      <PrivacyPolicy />
    </main>
    <SiteFooter />
  </StrictMode>,
);
