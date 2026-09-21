import { CONTACT_EMAIL, EXPERIENCE, TOOLKIT } from "@/lib/content";
import type { ReactNode } from "react";

/** Renders the **bold** spans used in the experience bullets. */
function emphasise(text: string): ReactNode[] {
  return text.split(/\*\*(.+?)\*\*/g).map((part, i) =>
    i % 2 === 1 ? <b key={i}>{part}</b> : <span key={i}>{part}</span>,
  );
}

export function Experience() {
  return (
    <section className="band" id="experience">
      <div className="band__head">
        <h2>Experience</h2>
      </div>

      <div className="exp-grid">
        {EXPERIENCE.map((e) => (
          <article className="exp" key={e.role + e.organisation}>
            <div className="exp__row">
              <h3>{e.role}</h3>
              <span className="when">{e.period}</span>
            </div>
            <p className="where">
              {e.organisation} <span>· {e.location}</span>
            </p>
            <ul>
              {e.bullets.map((b, i) => (
                <li key={i}>{emphasise(b)}</li>
              ))}
            </ul>
          </article>
        ))}
      </div>
    </section>
  );
}

export function Toolkit() {
  return (
    <section className="band" id="toolkit">
      <div className="band__head">
        <h2>Toolkit</h2>
      </div>
      <div className="kit">
        {TOOLKIT.map((group) => (
          <div key={group.heading}>
            <h3>{group.heading}</h3>
            <div className="stack">
              {group.items.map((item) => (
                <span key={item}>{item}</span>
              ))}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export function Education() {
  return (
    <section className="band" id="education">
      <div className="band__head">
        <h2>Education</h2>
      </div>
      <div className="edu">
        <div>
          <h3>University of Waterloo</h3>
          <p className="where">BASc, Computer Engineering · GPA 93%</p>
        </div>
        <span className="when">Sept 2025 – May 2030 · Waterloo, Canada</span>
      </div>
    </section>
  );
}

export function CallToAction() {
  return (
    <section className="cta">
      <h2>Want to Work Together?</h2>
      <p className="muted">Currently looking for Summer 2027 co-op. Contact me and we can chat.</p>
      <a className="btn" href="#contact">
        Contact Me
      </a>
    </section>
  );
}

export function Contact() {
  return (
    <section className="contact" id="contact">
      <h2>Contact Me</h2>

      <div className="field">
        <label htmlFor="email">Your email</label>
        <input id="email" type="email" placeholder="you@company.com" autoComplete="email" />
      </div>

      <div className="field">
        <label htmlFor="msg">Your message</label>
        <textarea id="msg" placeholder="Tell me what you're building." />
      </div>

      <button className="btn" type="button">
        Send message
      </button>

      <p className="note">
        Not wired up yet — the contact endpoint arrives with the backend. Email works:{" "}
        <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>
      </p>
    </section>
  );
}
