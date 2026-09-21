import { PROJECT_BADGES, PROJECT_METRICS } from "@/lib/content";
import type { Project } from "@/lib/types";
import { ArchitectureDialog } from "./architecture-dialog";
import { ArrowRight } from "./icons";

interface Props {
  project: Project;
  /** The lead card runs full width and lays its thumbnail alongside the text. */
  wide?: boolean;
}

export function ProjectCard({ project, wide = false }: Props) {
  const metrics = PROJECT_METRICS[project.slug] ?? [];
  const badge = PROJECT_BADGES[project.slug];

  return (
    <article className={wide ? "work work--wide" : "work"}>
      <div className="thumb">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={`/diagrams/${project.slug}.svg`}
          alt={`Diagram of what ${project.name} does`}
          width={640}
          height={wide ? 400 : 360}
        />
        {badge ? <span className="thumb__tag">{badge}</span> : null}
      </div>

      <div className="work__body">
        <h3>{project.name}</h3>
        <p className="desc">{project.description}</p>

        {metrics.length > 0 ? (
          <dl className="stats">
            {metrics.map((m) => (
              <div className="stat" key={m.label}>
                <dt>{m.label}</dt>
                <dd>
                  {m.value}
                  {m.unit ? <small>{m.unit}</small> : null}
                </dd>
              </div>
            ))}
          </dl>
        ) : null}

        {project.technologies.length > 0 ? (
          <div className="stack">
            {project.technologies.map((t) => (
              <span key={t}>{t}</span>
            ))}
          </div>
        ) : null}

        <div className="actions">
          {project.githubUrl ? (
            <a className="btn" href={project.githubUrl}>
              GitHub <ArrowRight />
            </a>
          ) : null}
          {project.demoUrl ? (
            <a className="btn btn--ghost" href={project.demoUrl}>
              Demo <ArrowRight />
            </a>
          ) : null}
          {project.slug === "navigation-assistant" ? <ArchitectureDialog /> : null}
        </div>
      </div>
    </article>
  );
}
