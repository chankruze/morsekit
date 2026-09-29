import { createBrowserRouter } from "react-router";
import { RouterProvider } from "react-router/dom";
import { ROUTER_BASENAME } from "@/constants/routes";
import { APP_ROUTES } from "@/providers/app-routes";

// privacy/index.html also loads this app, so /privacy/ works as a direct link (Play Console) on
// GitHub Pages, which has no server-side routing.
const router = createBrowserRouter(APP_ROUTES, { basename: ROUTER_BASENAME });

export const AppRouter = () => <RouterProvider router={router} />;
