"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";
import { ThemeProvider } from "@/components/theme-provider";

export function Providers({ children }: { children: React.ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 30_000,
            retry: (failureCount, error) => {
              if (error instanceof Error && error.message.startsWith("401")) return false;
              return failureCount < 1;
            },
          },
        },
      })
  );

  return (
    <QueryClientProvider client={client}>
      <ThemeProvider defaultTheme="light">{children}</ThemeProvider>
    </QueryClientProvider>
  );
}
