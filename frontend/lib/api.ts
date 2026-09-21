import { FALLBACK_PROJECTS } from "./content";
import type { BlogPostSummary, Project } from "./types";

/**
 * Server-side rendering talks to the backend directly over the container
 * network; the browser uses the public URL. Both fall back to localhost so a
 * developer with `docker compose up` needs no configuration.
 */
const BASE_URL =
  process.env.API_BASE_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080";

/** Section 76: the page must render even when the backend does not answer. */
const TIMEOUT_MS = 2500;

async function get<T>(path: string, fallback: T, revalidate: number): Promise<T> {
  try {
    const response = await fetch(`${BASE_URL}${path}`, {
      // Route-level caching (spec section 3). A portfolio changes rarely, so
      // there is no reason to hit the backend on every request.
      next: { revalidate },
      signal: AbortSignal.timeout(TIMEOUT_MS),
      headers: { Accept: "application/json" },
    });

    if (!response.ok) {
      console.warn(`[api] ${path} returned ${response.status}; using fallback content`);
      return fallback;
    }

    return (await response.json()) as T;
  } catch (error) {
    // A cold VPS, a DNS blip or no backend at all. None of these should be a
    // blank page, so this is a warning rather than a thrown error.
    const reason = error instanceof Error ? error.message : String(error);
    console.warn(`[api] ${path} unreachable (${reason}); using fallback content`);
    return fallback;
  }
}

export function getProjects(): Promise<Project[]> {
  return get<Project[]>("/api/v1/projects?featured=true", FALLBACK_PROJECTS, 300);
}

export function getPosts(): Promise<BlogPostSummary[]> {
  return get<BlogPostSummary[]>("/api/v1/posts", [], 600);
}
