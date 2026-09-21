import type { Metadata } from "next";
import { Chakra_Petch, Inter } from "next/font/google";
import "./globals.css";

// next/font self-hosts these at build time, so there is no request to Google
// at runtime and no flash of unstyled text.
const display = Chakra_Petch({
  subsets: ["latin"],
  weight: ["600", "700"],
  variable: "--font-display",
  display: "swap",
});

const body = Inter({
  subsets: ["latin"],
  variable: "--font-body",
  display: "swap",
});

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL ?? "https://leonzhang.dev";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: "Leon Zhang | Computer Engineering @ Waterloo",
  description:
    "Computer Engineering at the University of Waterloo. On-device machine learning, computer vision, and the perception pipelines around them.",
  openGraph: {
    title: "Leon Zhang",
    description:
      "On-device machine learning, computer vision, and the perception pipelines around them.",
    url: SITE_URL,
    siteName: "Leon Zhang",
    type: "website",
  },
  twitter: { card: "summary_large_image" },
  robots: { index: true, follow: true },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={`${display.variable} ${body.variable}`}>
      <body>{children}</body>
    </html>
  );
}
