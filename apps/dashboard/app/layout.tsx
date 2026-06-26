import type { Metadata } from "next";
import { GoogleAnalytics } from "@next/third-parties/google";
import { Inter, JetBrains_Mono } from "next/font/google";
import "./globals.css";
import { Providers } from "@/components/providers";
import { siteConfig } from "@/lib/site-config";

const GA_MEASUREMENT_ID =
  process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID?.trim() || "";

const inter = Inter({
  variable: "--font-sans",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
});

const jetbrainsMono = JetBrains_Mono({
  variable: "--font-mono",
  subsets: ["latin"],
  weight: ["400", "500"],
});

export const metadata: Metadata = {
  metadataBase: new URL(siteConfig.url),
  title: {
    default: siteConfig.name,
    template: `%s | ${siteConfig.name}`,
  },
  description: siteConfig.description,
  keywords: [
    "EU AI Act",
    "AI governance",
    "AI assurance",
    "conformity assessment",
    "RAG evidence",
    "eval gates",
    "AI risk classification",
    "audit trail",
    "EU AI Assurance OS",
  ],
  authors: [{ name: "Marti Soura Vamseekar", url: siteConfig.url }],
  alternates: {
    canonical: siteConfig.url,
  },
  openGraph: {
    title: siteConfig.name,
    description: siteConfig.description,
    siteName: siteConfig.name,
    locale: siteConfig.locale,
    type: "website",
    url: siteConfig.url,
    images: [{ url: "/opengraph-image", width: 1200, height: 630 }],
  },
  twitter: {
    card: "summary_large_image",
    title: siteConfig.name,
    description: siteConfig.description,
    images: ["/opengraph-image"],
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-image-preview": "large",
      "max-snippet": -1,
      "max-video-preview": -1,
    },
  },
  verification: {
    google: "SXR8A2bh2lD8pBJKLNQRJFVmqut4pZvSdGbsKon4SNQ",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en-GB"
      suppressHydrationWarning
      className={`${inter.variable} ${jetbrainsMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        <Providers>{children}</Providers>
        {GA_MEASUREMENT_ID && process.env.NODE_ENV === "production" ? (
          <GoogleAnalytics gaId={GA_MEASUREMENT_ID} />
        ) : null}
      </body>
    </html>
  );
}
