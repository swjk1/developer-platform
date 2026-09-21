export type ProjectStatus = "ACTIVE" | "ARCHIVED" | "IN_PROGRESS";

/** Mirrors ProjectResponse on the backend. Kept in sync by the contract test in CI. */
export interface Project {
  slug: string;
  name: string;
  description: string;
  longDescription: string | null;
  status: ProjectStatus;
  featured: boolean;
  githubUrl: string | null;
  demoUrl: string | null;
  technologies: string[];
}

export interface BlogPostSummary {
  slug: string;
  title: string;
  summary: string | null;
  publishedAt: string | null;
}

/** A measured figure shown on a project card. Sourced, never invented. */
export interface Metric {
  label: string;
  value: string;
  unit?: string;
}

export interface Experience {
  role: string;
  organisation: string;
  location: string;
  period: string;
  bullets: string[];
}
