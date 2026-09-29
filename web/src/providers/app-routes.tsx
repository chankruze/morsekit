import { Navigate, type RouteObject } from "react-router";
import { ROUTES } from "@/constants/routes";
import { SiteLayout } from "@/layouts/site-layout";
import { HomeScreen } from "@/screens/home/screen";
import { PrivacyScreen } from "@/screens/privacy/screen";

export const APP_ROUTES: RouteObject[] = [
  {
    element: <SiteLayout />,
    children: [
      { index: true, element: <HomeScreen /> },
      { path: ROUTES.privacy, element: <PrivacyScreen /> },
      { path: "*", element: <Navigate to={ROUTES.home} replace /> },
    ],
  },
];
