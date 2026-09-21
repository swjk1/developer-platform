# Personal Developer Platform

A deployed personal portfolio backed by a production-style service architecture
rather than a static frontend. The website is the visible product; the
infrastructure behind it is the project.

The full specification is in [SPEC.md](./SPEC.md); section references
throughout this README (§n) point at it.

```
        Internet
            │
            ▼
   Next.js frontend  ──HTTPS──▶  Spring Boot API  ──┬──▶ PostgreSQL
   (Vercel)                      (VPS, Docker)      └──▶ Redis
```

## Status

| Phase | Scope | State |
|---|---|---|
| 1 | Spring Boot, PostgreSQL, Flyway, Projects API, Blog API, Docker Compose | **done** |
| 2 | JUnit, Testcontainers, REST Assured, GitHub Actions | **done** |
| 3 | GitHub sync: API client, scheduled job, retry, cached fallback | not started |
| 4 | Redis: cache-aside, invalidation, rate limiting | not started |
| 5 | Analytics: event ingestion, Redis Streams, idempotent worker, aggregation | not started |
| 6 | Auth: JWT, refresh tokens, admin API | not started |
| 7 | Playwright across Chrome, Firefox, mobile viewports | not started |
| 8 | `testctl` harness and deterministic seeding | not started |
| 9 | k6, Actuator dashboards, structured logs | partial — request ids and Actuator wired |

The frontend is deployed. The backend runs locally under Docker Compose and is
not yet hosted, so the live site currently renders from its fallback content —
see [Graceful degradation](#graceful-degradation).

## Layout

```
├── frontend/          Next.js 16, TypeScript, Tailwind 4
├── backend/           Spring Boot 3, Java 21
│   └── src/main/java/com/leon/
│       ├── project/   Projects API
│       ├── blog/      Blog API
│       ├── common/    request ids, error shape
│       └── config/    CORS
├── infrastructure/    docker-compose.yml, Caddy
├── tests/             e2e, performance, contracts (phases 7–9)
├── design/            the original static mockup this was built from
└── .github/workflows/ backend-ci, frontend-ci
```

## Running it

Everything, as it runs in production:

```bash
docker compose -f infrastructure/docker-compose.yml up --build
```

- frontend → http://localhost:3000
- backend → http://localhost:8080
- health → http://localhost:8080/actuator/health

Frontend alone (no backend needed — it falls back):

```bash
cd frontend && npm install && npm run dev
```

Backend tests:

```bash
cd backend
mvn test      # unit only, no containers, seconds
mvn verify    # + Testcontainers integration and REST Assured API tests
```

`mvn verify` starts a real PostgreSQL 17 through Testcontainers, so Docker must
be running. The migration chain is applied to that container on every run,
which means the migrations themselves are under test.

No local JDK is required to *run* the stack — the backend image builds Maven
and the JDK in its own build stage. A JDK 21 is only needed to run `mvn`
directly.

## Design decisions worth knowing

**The engine of the site is the database, not the code.** Projects come from
PostgreSQL (spec §6) rather than being hardcoded in the frontend. The three
featured projects are seeded by `V4__seed_portfolio_content.sql`.

**Flyway owns the schema; Hibernate is set to `validate`.** Hibernate may check
that the mapping still matches, never change it. Every environment runs the
identical migration chain.

**The blog search vector exists but is deliberately unindexed.** §18 wants
PostgreSQL full-text search and §62 wants a measured before-and-after from
adding a GIN index. Creating the `tsvector` column now and the index later means
that improvement can be *measured* rather than asserted.

**Integration tests use real PostgreSQL, not H2.** The schema relies on
`tsvector`, generated columns and check constraints that an in-memory
substitute would silently not enforce. `SchemaConstraintsIT` asserts the
database rejects a published post with no publication date, a duplicate slug,
and an unknown status — the kinds of thing a future service method might forget.

**Surefire runs `*Test`, Failsafe runs `*IT`.** `mvn test` stays fast for local
work; `mvn verify` runs the whole pyramid.

<a id="graceful-degradation"></a>
**The site renders when the backend does not.** `lib/api.ts` has a 2.5 s
timeout and falls back to local content on any failure. A portfolio showing an
error page because a VPS is rebooting is worse than one showing slightly stale
content. This path is exercised on every CI build, which runs with no backend
reachable.

**Metrics live in the frontend, not the database.** Every figure on a project
card is traceable to a document in that project's repository; the source is
noted beside each entry in `lib/content.ts`. Nothing is shown that is not
measured.

## Deployment

**Frontend** — Vercel, from `frontend/`. Set `NEXT_PUBLIC_API_BASE_URL` and
`NEXT_PUBLIC_SITE_URL` once the backend has a hostname.

**Backend** — one small VPS running the same compose file, with Caddy
terminating TLS. Caddy obtains and renews Let's Encrypt certificates
automatically, and blocks every `/actuator/*` path except `/actuator/health`.

Nothing in this repository is host-specific; moving providers means changing a
hostname.

## Configuration

Secrets come from the environment and are never committed (§75).

| Variable | Used by | Purpose |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | backend | PostgreSQL connection |
| `CORS_ALLOWED_ORIGINS` | backend | comma-separated allowed origins |
| `SPRING_PROFILES_ACTIVE` | backend | `local` or `prod` |
| `NEXT_PUBLIC_API_BASE_URL` | frontend | backend URL as seen by the browser |
| `API_BASE_URL` | frontend | backend URL as seen by the server renderer |
| `NEXT_PUBLIC_SITE_URL` | frontend | canonical URL for metadata |
