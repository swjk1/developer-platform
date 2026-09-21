import type { Experience, Metric, Project } from "./types";

/**
 * Fallback content, used when the API is unreachable.
 *
 * The backend is the source of truth (spec section 6), but the site must still
 * render if the backend is down or not yet deployed — a portfolio that shows an
 * error page because a VPS is rebooting is worse than one showing slightly
 * stale content. This is the same shape the API returns.
 */
export const FALLBACK_PROJECTS: Project[] = [
  {
    slug: "navigation-assistant",
    name: "Navigation Assistant for the Visually Impaired",
    description:
      "An Android app that guides a blind or low-vision user through an indoor space it has never seen — no floor plan, no beacons, no installed hardware. It maps the room with ARCore depth, reads signs and obstacles on device, and gives one instruction at a time.",
    longDescription: null,
    status: "ACTIVE",
    featured: true,
    githubUrl: "https://github.com/swjk1/Navigation-assistant-for-visually-impaired",
    demoUrl: null,
    technologies: ["Kotlin", "ARCore", "YOLO", "ONNX Runtime", "ML Kit OCR", "React Native"],
  },
  {
    slug: "keyguard",
    name: "Keyguard",
    description:
      "A safety keyboard and accessibility overlay that warns before a child sends something they shouldn't — personal information, or content that could hurt them. Detection runs on device; nothing typed ever leaves the phone.",
    longDescription: null,
    status: "ACTIVE",
    featured: true,
    githubUrl: "https://github.com/swjk1/keyguard",
    demoUrl: null,
    technologies: ["Kotlin", "PyTorch", "ONNX Runtime", "Android", "Next.js"],
  },
  {
    slug: "cookpilot",
    name: "CookPilot — Hands-Free Recipe Assistant",
    description:
      "A hands-free cooking assistant. Instead of pausing a video with wet hands you talk to it — it has already watched the recipe, pulled out the steps, and reads you one at a time.",
    longDescription: null,
    status: "ACTIVE",
    featured: true,
    githubUrl: "https://github.com/swjk1/CookBot",
    demoUrl: null,
    technologies: ["Python", "Whisper", "PaddleOCR", "PostgreSQL", "Docker", "Railway"],
  },
];

/**
 * Metrics are presentation, not data, so they live in the frontend rather than
 * the projects table. Every figure below is traceable to a document in the
 * corresponding repository — see the note in each entry.
 */
export const PROJECT_METRICS: Record<string, Metric[]> = {
  // TECHNICAL.md section 4.1, validation split, best epoch 41.
  "navigation-assistant": [
    { label: "Detector mAP50", value: "0.833" },
    { label: "Precision", value: "0.919" },
    { label: "On-device model", value: "9.8", unit: "MB" },
  ],
  // README GoldenCorpusTest output, plus the module test counts (61+235+16).
  keyguard: [
    { label: "Median scan, 500 chars", value: "136", unit: "µs" },
    { label: "PII entity types", value: "23" },
    { label: "Tests", value: "312" },
  ],
  // No benchmark exists in that repository yet, so no tiles are shown.
  cookpilot: [],
};

export const PROJECT_BADGES: Record<string, string> = {
  "navigation-assistant": "mAP50 0.833 · 17 classes",
  keyguard: "On device · no network",
  cookpilot: "Hands-free · voice",
};

export const EXPERIENCE: Experience[] = [
  {
    role: "Undergraduate Research Assistant",
    organisation: "Vision and Image Processing Lab",
    location: "Waterloo, Canada",
    period: "Sept 2026 – Present",
    bullets: [
      "Built a **puck-tracking pipeline** that converts noisy broadcast-video detections into rink-space position and velocity tracks, using homography projection, calibrated confidence gating, local outlier rejection, and possession-based anchoring.",
      "Implemented a segmented constant-velocity **Kalman filter with a Rauch–Tung–Striebel smoother**, Mahalanobis gating and uncertainty estimation, cutting physically impossible >50 m/s jumps from **35.8% to 1.3%**.",
      "Evaluated on 62 held-out segments and 81k+ frames: median puck-position error fell from **2.99 m to 0.71 m** at 82.6% valid-frame coverage.",
    ],
  },
  {
    role: "Software Engineer",
    organisation: "Unmodal Research",
    location: "Toronto, Canada",
    period: "May 2026 – Sept 2026",
    bullets: [
      "Built a FastAPI/LangGraph **database migration platform** mapping 25,000+ source fields across 3,000+ tables into a standardized common data model.",
      "Saved **8+ hours a week** of project-management triage with an agent that ingests meeting transcripts, extracts action items, and creates and updates assigned Trello tasks.",
      "Built and presented a custom restaurant ordering application from client requirements, iterating directly with stakeholders; **now being deployed to restaurants** in the U.S. and Middle East.",
    ],
  },
];

export const TOOLKIT = [
  { heading: "Languages", items: ["Python", "C++", "Kotlin", "TypeScript", "JavaScript", "SQL"] },
  { heading: "Frameworks & Libraries", items: ["PyTorch", "ONNX Runtime", "FastAPI", "LangGraph", "React", "Next.js"] },
  { heading: "Tools & Platforms", items: ["Git", "Docker", "Linux", "Railway", "PostgreSQL", "ARCore"] },
];

export const CONTACT_EMAIL = "leonzh1018@gmail.com";
export const GITHUB_URL = "https://github.com/swjk1";
export const LINKEDIN_URL = "https://www.linkedin.com/in/leon-zhang-zijun";
