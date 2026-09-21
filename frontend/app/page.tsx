import { ProjectCard } from "@/components/project-card";
import { CallToAction, Contact, Education, Experience, Toolkit } from "@/components/sections";
import { SocialLinks } from "@/components/social-links";
import { getProjects } from "@/lib/api";

export default async function Home() {
  const projects = await getProjects();
  const [lead, ...rest] = projects;

  return (
    <div className="wrap">
      <header className="top">
        <div>
          <h1>Leon Zhang, Computer Engineering @ Waterloo</h1>
          <p>On-device machine learning and the perception pipelines around it</p>
        </div>
        <SocialLinks />
      </header>

      <p className="intro">
        <b>Computer Engineering at the University of Waterloo.</b> I&rsquo;m interested in on-device
        machine learning, computer vision, and the perception pipelines around them.
      </p>

      <section className="band" id="projects">
        <div className="band__head">
          <h2>Featured Projects</h2>
        </div>

        <div className="work-grid">
          {lead ? <ProjectCard project={lead} wide /> : null}
          {rest.map((p) => (
            <ProjectCard project={p} key={p.slug} />
          ))}
        </div>
      </section>

      <CallToAction />
      <Experience />
      <Toolkit />
      <Education />
      <Contact />

      <footer>
        <SocialLinks />
        <p className="fine">Leon Zhang · Waterloo, Ontario</p>
      </footer>
    </div>
  );
}
