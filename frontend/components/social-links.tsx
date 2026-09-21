import { CONTACT_EMAIL, GITHUB_URL, LINKEDIN_URL } from "@/lib/content";
import { GitHubIcon, LinkedInIcon, MailIcon } from "./icons";

/*
 * No résumé link yet, deliberately. The PDF carries a personal phone number,
 * and a file served from a public origin gets scraped. Restoring it is two
 * lines: drop the file at frontend/public/resume.pdf and add the ResumeIcon
 * entry back — ideally a copy with the phone number removed.
 */
export function SocialLinks() {
  return (
    <nav className="icons" aria-label="Elsewhere">
      <a href={GITHUB_URL} title="GitHub" aria-label="GitHub">
        <GitHubIcon />
      </a>
      <a href={LINKEDIN_URL} title="LinkedIn" aria-label="LinkedIn">
        <LinkedInIcon />
      </a>
      <a href={`mailto:${CONTACT_EMAIL}`} title="Email" aria-label="Email">
        <MailIcon />
      </a>
    </nav>
  );
}
