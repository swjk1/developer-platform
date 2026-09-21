# Personal Developer Platform

> The specification this repository is built against. Phase-by-phase
> implementation status is tracked in [README.md](./README.md#status).

## 1. Project Overview

### 1.1 Objective

Build a deployed personal portfolio website backed by a production-style service architecture rather than a static frontend.

The platform should demonstrate:

* REST API design
* Java/Spring backend development
* Relational database design
* Caching
* Third-party API integration
* Asynchronous/background processing
* Authentication and authorization
* Rate limiting
* Event ingestion and analytics
* Automated API and browser testing
* Test-data management
* Containerized integration testing
* CI/CD
* Performance testing
* Observability
* Failure handling

The public website is the visible product.

The primary technical project is the infrastructure behind it.

---

# 2. High-Level Architecture

```text
                         Internet
                            │
                            ▼
                 ┌────────────────────┐
                 │ Next.js Frontend   │
                 │ leonzhang.dev      │
                 └─────────┬──────────┘
                           │
                        HTTPS
                           │
                           ▼
                 ┌────────────────────┐
                 │ Spring Boot API    │
                 │                    │
                 │ Projects           │
                 │ Blog               │
                 │ Search             │
                 │ Resume             │
                 │ Analytics          │
                 │ Contact            │
                 │ Authentication     │
                 │ Admin              │
                 └──────┬───────┬─────┘
                        │       │
                    SQL │       │ Cache
                        ▼       ▼
                 PostgreSQL   Redis
                        ▲       ▲
                        │       │
             ┌──────────┘       │
             │                  │
             ▼                  │
      Background Workers ───────┘
             │
             ▼
         GitHub API
```

Supporting infrastructure:

```text
                    GitHub Repository
                           │
                           ▼
                     GitHub Actions
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
       Unit Tests     Integration Tests   Static Analysis
                           │
                           ▼
                    Testcontainers
                  PostgreSQL + Redis
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
       API Tests      Browser Tests    Contract Tests
     REST Assured      Playwright
                           │
                           ▼
                    Performance Tests
                           │
                           ▼
                     Deployment
```

---

# 3. Technology Stack

## Frontend

**Next.js + TypeScript**

Use Next.js rather than a fully static React application so the website can support:

* server-side rendering
* SEO
* API consumption
* route-level caching
* metadata generation

Recommended libraries:

* React
* TypeScript
* Tailwind CSS
* TanStack Query

---

## Backend

**Java 21**

**Spring Boot 3**

Modules:

* Spring Web
* Spring Data JPA
* Spring Security
* Spring Validation
* Spring Actuator
* Spring Scheduling
* Spring Retry
* Flyway
* Jackson

---

## Database

**PostgreSQL**

Use for:

* projects
* blog posts
* GitHub metadata
* contact submissions
* analytics events
* analytics aggregates
* user/admin data
* refresh tokens

---

## Cache / Queue

**Redis**

Use for:

* API caching
* rate limiting
* distributed locks
* analytics queue
* GitHub sync state

Optional:

Redis Streams for asynchronous analytics processing.

---

## Testing

Backend:

```text
JUnit 5
Mockito
Testcontainers
REST Assured
jqwik
```

Frontend / E2E:

```text
Playwright
```

Performance:

```text
k6
```

---

## Infrastructure

```text
Docker
Docker Compose
GitHub Actions
```

Deployment could use:

```text
Frontend: Vercel

Backend:
Railway
Render
Fly.io
AWS
Azure
```

The specific cloud provider matters less than having a deployed, monitored service.

---

# 4. Repository Structure

Recommended monorepo:

```text
developer-platform/
│
├── backend/
│   ├── src/
│   │   ├── main/java/com/leon/
│   │   │   ├── project/
│   │   │   ├── blog/
│   │   │   ├── github/
│   │   │   ├── analytics/
│   │   │   ├── contact/
│   │   │   ├── search/
│   │   │   ├── auth/
│   │   │   ├── admin/
│   │   │   ├── common/
│   │   │   └── config/
│   │   │
│   │   └── test/java/com/leon/
│   │       ├── unit/
│   │       ├── integration/
│   │       ├── api/
│   │       └── concurrency/
│   │
│   ├── src/main/resources/
│   │   └── db/migration/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── app/
│   ├── components/
│   ├── lib/
│   └── package.json
│
├── tests/
│   ├── e2e/
│   ├── performance/
│   └── contracts/
│
├── test-platform/
│   ├── testctl/
│   ├── seed/
│   └── reporting/
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── prometheus/
│   └── grafana/
│
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       ├── frontend-ci.yml
│       └── deploy.yml
│
└── README.md
```

---

# 5. Core Backend Services

The backend should contain the following services:

```text
ProjectService
BlogService
GitHubSyncService
AnalyticsService
SearchService
ContactService
AuthService
CacheService
```

Each service should be independently testable.

---

# 6. Projects API

Projects shown on the portfolio should come from PostgreSQL rather than being hardcoded into the frontend.

## Project Model

```text
Project
-------
id
slug
name
short_description
long_description
status
featured
github_url
demo_url
display_order
created_at
updated_at
```

Possible status values:

```text
ACTIVE
ARCHIVED
IN_PROGRESS
```

---

## Public Endpoints

```http
GET /api/v1/projects
```

Optional filters:

```http
GET /api/v1/projects?featured=true
GET /api/v1/projects?status=ACTIVE
```

Specific project:

```http
GET /api/v1/projects/{slug}
```

Example:

```http
GET /api/v1/projects/navigation-assistant
```

Response:

```json
{
  "slug": "navigation-assistant",
  "name": "Indoor Navigation Assistant",
  "description": "Real-time indoor navigation system...",
  "technologies": [
    "Kotlin",
    "ARCore",
    "ONNX Runtime"
  ],
  "github": {
    "repository": "swjk1/navigation-assistant",
    "stars": 12,
    "primaryLanguage": "Kotlin",
    "lastCommit": "2026-09-19T21:42:00Z"
  }
}
```

---

# 7. Admin Project API

Protected routes:

```http
POST   /api/v1/admin/projects
PATCH  /api/v1/admin/projects/{id}
DELETE /api/v1/admin/projects/{id}
```

Only authenticated admin users can access them.

Example:

```http
Authorization: Bearer <JWT>
```

---

# 8. Blog System

Add a lightweight technical blog.

This makes the backend more useful while providing additional API and search surface.

## BlogPost Model

```text
BlogPost
--------
id
slug
title
summary
content
status
published_at
created_at
updated_at
```

Statuses:

```text
DRAFT
PUBLISHED
ARCHIVED
```

Endpoints:

```http
GET /api/v1/posts
GET /api/v1/posts/{slug}
```

Admin:

```http
POST  /api/v1/admin/posts
PATCH /api/v1/admin/posts/{id}
```

---

# 9. GitHub Synchronization System

This should be one of the central backend features.

The backend periodically synchronizes metadata from GitHub.

Architecture:

```text
                  Scheduler
                     │
                     ▼
              GitHubSyncJob
                     │
                     ▼
                 GitHub API
                     │
                     ▼
              Response Parser
                     │
                     ▼
           GitHubRepositoryMetadata
                     │
                     ▼
                PostgreSQL
                     │
                     ▼
             Cache Invalidation
```

---

# 10. GitHub Metadata

Store:

```text
GitHubRepository
----------------
id
project_id
owner
repository
stars
forks
open_issues
primary_language
last_commit_at
last_release
last_synced_at
sync_status
```

---

# 11. Scheduled GitHub Sync

Example:

```java
@Scheduled(fixedDelayString = "${github.sync.interval}")
public void synchronizeRepositories() {
    ...
}
```

Run approximately every:

```text
30 minutes
```

Avoid syncing every website request.

---

# 12. GitHub API Failure Handling

The external service should not be assumed reliable.

Handle:

```text
403 rate limit
429 rate limit
500 GitHub error
network timeout
DNS failure
invalid repository
```

Use:

```text
exponential backoff
+
retry jitter
```

Example:

```text
Attempt 1

500 ms

Attempt 2

1 second

Attempt 3

2 seconds
```

After retry exhaustion:

```text
keep previously cached metadata
```

The website should continue operating even if GitHub is unavailable.

---

# 13. Circuit Breaker

Optional advanced feature.

Use Resilience4j.

State:

```text
CLOSED
    │
errors
    ▼
OPEN
    │
timeout
    ▼
HALF_OPEN
```

When GitHub repeatedly fails, stop sending requests temporarily.

This gives you a useful distributed-systems discussion point.

---

# 14. Redis Caching

Implement cache-aside caching.

Example request:

```http
GET /api/v1/projects
```

Flow:

```text
Client
  │
  ▼
Spring Boot
  │
  ▼
Redis
  │
  ├── HIT
  │      │
  │      ▼
  │   Response
  │
  └── MISS
         │
         ▼
     PostgreSQL
         │
         ▼
        Redis
         │
         ▼
      Response
```

---

# 15. Cache Keys

Example:

```text
projects:all

projects:featured

project:navigation-assistant

post:building-indoor-navigation

github:swjk1/navigation-assistant
```

---

# 16. Cache Expiration

Example TTLs:

```text
Project metadata:
5 minutes

GitHub metadata:
30 minutes

Blog post:
10 minutes
```

---

# 17. Cache Invalidation

When admin modifies project:

```text
PATCH /admin/projects/123
```

perform:

```text
1. update PostgreSQL

2. delete:
   project:{slug}

3. delete:
   projects:all

4. delete:
   projects:featured
```

This demonstrates understanding of cache consistency.

---

# 18. Search API

Implement website-wide search.

Endpoint:

```http
GET /api/v1/search?q=kalman
```

Search:

```text
projects

blog posts

technologies
```

Use PostgreSQL full-text search.

Avoid Elasticsearch initially.

---

# 19. Search Architecture

```text
Search Query
     │
     ▼
SearchService
     │
     ▼
PostgreSQL
     │
     ▼
tsvector index
     │
     ▼
Ranked Results
```

Example response:

```json
[
  {
    "type": "PROJECT",
    "title": "Hockey Puck Tracker",
    "slug": "hockey-puck-tracker",
    "score": 0.83
  },
  {
    "type": "BLOG",
    "title": "Tracking Through Occlusion",
    "slug": "tracking-through-occlusion",
    "score": 0.61
  }
]
```

---

# 20. Custom Analytics Platform

Do not rely entirely on Google Analytics.

Create your own simple first-party analytics system.

The goal is engineering experience, not marketing analytics.

---

# 21. Event Ingestion API

Endpoint:

```http
POST /api/v1/events
```

Request:

```json
{
  "eventId": "01995ab2-70fc-7cb2-aabc-220b2da07cf1",
  "type": "PROJECT_VIEW",
  "resource": "navigation-assistant",
  "timestamp": "2026-09-21T18:22:41Z"
}
```

Possible event types:

```text
PAGE_VIEW

PROJECT_VIEW

PROJECT_DEMO_CLICK

GITHUB_CLICK

RESUME_DOWNLOAD

BLOG_VIEW

CONTACT_SUBMIT
```

Do not collect unnecessary personally identifiable information.

---

# 22. Asynchronous Analytics Architecture

Rather than synchronously inserting every event into PostgreSQL:

```text
Browser
   │
   ▼
POST /events
   │
   ▼
Validation
   │
   ▼
Redis Stream
   │
   ▼
202 Accepted
```

Worker:

```text
Redis Stream
    │
    ▼
AnalyticsWorker
    │
    ▼
Deduplication
    │
    ▼
PostgreSQL
```

This makes analytics event processing asynchronous.

---

# 23. Analytics Event Model

```text
AnalyticsEvent
--------------
id
event_id
event_type
resource
session_id
created_at
processed_at
```

`event_id`:

```text
UNIQUE
```

to provide idempotent processing.

---

# 24. Analytics Aggregation

Raw events should be periodically aggregated.

Example table:

```text
DailyMetric
-----------
date
metric_type
resource
count
```

Example:

```text
2026-09-21

PROJECT_VIEW

navigation-assistant

47
```

Dashboard queries aggregated tables instead of scanning millions of raw events.

---

# 25. Idempotent Event Processing

Redis streams can redeliver messages.

Therefore:

```text
eventId = unique
```

Before processing:

```sql
INSERT INTO analytics_event (...)
ON CONFLICT (event_id)
DO NOTHING;
```

Thus:

```text
same event delivered twice

→ counted once
```

This is useful reliability engineering.

---

# 26. Contact Service

Endpoint:

```http
POST /api/v1/contact
```

Request:

```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "message": "..."
}
```

Flow:

```text
Request
   │
   ▼
Validation
   │
   ▼
Rate Limiting
   │
   ▼
Spam Detection
   │
   ▼
Database
   │
   ▼
Notification
```

---

# 27. Contact Validation

Validate:

```text
name length

email syntax

message length

maximum payload size

HTML/script injection
```

Example:

```text
message maximum:
5000 characters
```

---

# 28. Rate Limiting

Use Redis to enforce API rate limits.

Important endpoints:

```text
/contact

/auth/login

/events

/search
```

Example:

```text
POST /contact

5 requests
per IP
per 10 minutes
```

---

# 29. Token Bucket Algorithm

Store:

```text
rate:{endpoint}:{client}
```

Example:

```text
rate:contact:203.0.113.1
```

Each request consumes one token.

Tokens replenish over time.

Return:

```http
HTTP 429 Too Many Requests
```

when limit is exceeded.

---

# 30. Authentication System

Admin functionality requires authentication.

Use:

```text
access JWT

+

refresh token
```

Login:

```http
POST /api/v1/auth/login
```

Response:

```json
{
  "accessToken": "...",
  "expiresIn": 900
}
```

Refresh:

```http
POST /api/v1/auth/refresh
```

Logout:

```http
POST /api/v1/auth/logout
```

---

# 31. Token Strategy

Access token lifetime:

```text
15 minutes
```

Refresh token lifetime:

```text
7 days
```

Store refresh-token hash in PostgreSQL.

Do not store plaintext refresh tokens.

---

# 32. Authorization

Roles:

```text
USER
ADMIN
```

Public APIs:

```text
GET projects

GET posts

GET search

POST analytics events
```

Protected:

```text
POST projects

PATCH projects

DELETE projects

POST posts

view analytics dashboard
```

---

# 33. Resume Service

Rather than hardcoding a PDF URL, provide:

```http
GET /api/v1/resume
```

Response:

```json
{
  "version": "2026.09",
  "updatedAt": "2026-09-20T19:00:00Z",
  "downloadUrl": "..."
}
```

Optional:

track:

```text
RESUME_DOWNLOAD
```

analytics event.

---

# 34. Database Schema

Core tables:

```text
users

refresh_tokens

projects

project_technologies

github_repositories

blog_posts

analytics_events

daily_metrics

contact_messages
```

---

# 35. Flyway Migrations

Example:

```text
V1__create_users.sql

V2__create_projects.sql

V3__create_blog_posts.sql

V4__create_github_metadata.sql

V5__create_analytics.sql

V6__create_contact_messages.sql
```

Every environment should use the same migration chain.

---

# 36. Automated Testing Strategy

Testing should be a first-class component of the project.

Test pyramid:

```text
                  Browser E2E
                     /\
                    /  \
                   /----\
                  / API  \
                 /--------\
                /Integration\
               /------------\
              /     Unit     \
             /________________\
```

Approximate distribution:

```text
Unit:

55–65%

Integration/API:

25–35%

E2E:

5–10%

Performance:
small dedicated suite
```

---

# 37. Unit Tests

Test pure logic.

Examples:

```text
Project ranking

GitHub response parsing

Rate-limit calculations

Analytics event validation

Search query normalization

Auth token expiry

Cache key generation
```

Example:

```java
@Test
void shouldRejectContactMessageOverLimit() {
    ...
}
```

---

# 38. Testcontainers

Integration tests should use real external services.

Containers:

```text
PostgreSQL

Redis
```

Example:

```java
@Container
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:17");
```

Redis:

```java
@Container
static GenericContainer<?> redis =
    new GenericContainer<>("redis:7")
        .withExposedPorts(6379);
```

Spring Boot then runs against real infrastructure.

---

# 39. Repository Integration Testing

Example:

```text
Insert project

Read project

Update project

Search project

Delete project
```

Verify actual PostgreSQL behavior.

---

# 40. Cache Integration Testing

Test:

```text
Request 1

Redis MISS
PostgreSQL read

Request 2

Redis HIT
No PostgreSQL read
```

Then update project:

```text
cache invalidated
```

Verify next request returns updated data.

---

# 41. GitHub Synchronization Testing

Do not call GitHub during every automated test.

Mock the external HTTP endpoint using:

```text
WireMock
```

Test:

```text
200 successful response

403 rate limit

429

500

timeout

malformed JSON
```

Example behavior:

```text
GitHub fails

→ stale cached data remains

→ sync status marked FAILED

→ website still works
```

---

# 42. API Automation

Use REST Assured.

Example:

```java
given()
    .contentType("application/json")

.when()
    .get("/api/v1/projects")

.then()
    .statusCode(200)
    .body("size()", greaterThan(0));
```

---

# 43. API Test Cases

Projects:

```text
GET all projects

GET valid project

GET missing project

filter featured projects

invalid filter
```

Contact:

```text
valid submission

invalid email

empty message

oversized message

rate limit reached
```

Auth:

```text
valid login

wrong password

expired token

invalid signature

refresh token revoked
```

Analytics:

```text
valid event

duplicate event

invalid event type

missing event ID

oversized resource name
```

---

# 44. API Contract Testing

Generate:

```text
/openapi.json
```

from Spring.

Commit stable contract snapshots.

CI compares:

```text
previous OpenAPI

vs

current OpenAPI
```

Fail PR on breaking changes such as:

```text
field removed

response status changed

type changed

required field added

endpoint removed
```

---

# 45. Browser Testing

Use Playwright.

Test:

```text
Chrome

Firefox
```

Optional:

```text
WebKit
```

---

# 46. E2E Test: Portfolio

```text
Open homepage

↓

Projects load

↓

Select Navigation Assistant

↓

Project page opens

↓

GitHub metadata visible

↓

Click GitHub link
```

---

# 47. E2E Test: Search

```text
Enter:

Kalman

↓

Search API queried

↓

Relevant project displayed

↓

Click result

↓

Correct project opens
```

---

# 48. E2E Test: Contact

```text
Open contact form

↓

Fill name/email/message

↓

Submit

↓

Success message

↓

Verify backend stored submission
```

---

# 49. Responsive Browser Tests

Playwright device profiles:

```text
Desktop Chrome

Pixel 9

iPhone

Tablet
```

Verify:

```text
navigation

project cards

contact form

resume download
```

---

# 50. Test Data Management

Build deterministic test-data tooling.

CLI:

```bash
./testctl seed standard
```

Possible datasets:

```text
empty

minimal

standard

large

edge-cases
```

---

# 51. Standard Dataset

Create:

```text
10 projects

10 blog posts

1 admin account

100 analytics events

5 contact messages
```

---

# 52. Large Dataset

Create:

```text
1000 projects

10000 blog posts

1,000,000 analytics events
```

This enables:

```text
performance testing

pagination testing

database indexing tests
```

---

# 53. Edge-Case Dataset

Include:

```text
very long titles

Unicode

emoji

special characters

empty optional fields

archived projects

large blog posts
```

---

# 54. Test Reset

CLI:

```bash
./testctl reset
```

should:

```text
clear database

reapply migrations

seed desired scenario

clear Redis
```

This gives every test suite a deterministic environment.

---

# 55. Test Harness CLI

Implement:

```text
testctl
```

in Python.

Commands:

```bash
testctl start

testctl stop

testctl seed standard

testctl unit

testctl integration

testctl api

testctl e2e

testctl performance

testctl all
```

---

# 56. `testctl all`

Flow:

```text
1. Build containers

2. Start PostgreSQL

3. Start Redis

4. Start backend

5. Run health checks

6. Apply migrations

7. Seed data

8. Run backend tests

9. Run API tests

10. Run browser tests

11. Run performance smoke tests

12. Collect logs

13. Generate report
```

---

# 57. Example Test Report

```text
Developer Platform Validation
─────────────────────────────────

Unit Tests

148 / 148 PASS

Integration

37 / 37 PASS

API

54 / 54 PASS

Chrome E2E

18 / 18 PASS

Firefox E2E

18 / 18 PASS

Contract Tests

1 / 1 PASS

Performance Smoke

PASS

─────────────────────────────────

Total

276 passed

0 failed
```

---

# 58. Failure Diagnostics

If an API test fails:

```text
TEST FAILURE
─────────────────────────────

Test:

GitHubFallbackTest

Endpoint:

GET /api/v1/projects/navigation-assistant

Expected:

HTTP 200

Actual:

HTTP 500

External GitHub response:

429 Too Many Requests

Expected behavior:

return cached repository metadata

Application log:

github.sync.rate_limit

Cache:

MISS

Possible cause:

fallback cache not populated
```

This makes the test infrastructure itself part of the project.

---

# 59. Performance Testing

Use k6.

Targets:

```text
GET /projects

GET /projects/{slug}

GET /search

POST /events
```

---

# 60. Example Load Profile

```text
0–30 seconds

10 users

30–90 seconds

50 users

90–150 seconds

100 users

150–180 seconds

ramp down
```

---

# 61. Performance Metrics

Track:

```text
requests/sec

p50 latency

p95 latency

p99 latency

error rate
```

Example goals:

```text
GET /projects

p95 < 100 ms
```

```text
POST /events

p95 < 150 ms
```

under moderate simulated load.

---

# 62. Database Performance Testing

Test:

```text
search with 10 posts

search with 10,000 posts

analytics with 1 million events
```

Compare:

```text
before index

vs

after index
```

Example:

```text
search query:

340 ms

after GIN index:

28 ms
```

This produces a meaningful optimization story.

---

# 63. Observability

Expose:

```text
/actuator/health

/actuator/metrics
```

Do not expose unrestricted admin endpoints publicly.

---

# 64. Request IDs

Every incoming request receives:

```text
X-Request-ID
```

Example:

```text
1d4eb5ac-f203-4e78-b506-14dff06a93af
```

Include in every log message.

---

# 65. Structured Logging

Example:

```json
{
  "timestamp": "2026-09-21T18:42:00Z",
  "level": "INFO",
  "requestId": "1d4eb5ac",
  "method": "GET",
  "path": "/api/v1/projects",
  "status": 200,
  "durationMs": 24
}
```

---

# 66. Application Metrics

Track:

```text
http.requests.total

http.request.duration

http.errors.5xx

cache.hit

cache.miss

github.sync.success

github.sync.failure

analytics.queue.depth

analytics.events.processed

contact.submissions
```

---

# 67. Health Checks

Application health should depend on critical services.

```text
Backend

PostgreSQL

Redis
```

Example:

```json
{
  "status": "UP",
  "components": {
    "postgres": "UP",
    "redis": "UP"
  }
}
```

GitHub should probably not make the whole service unhealthy because cached data can be used.

---

# 68. Failure Injection

Add controlled testing modes in non-production environments.

Examples:

```text
simulate GitHub 500

simulate Redis outage

simulate database latency

simulate worker crash
```

Verify graceful degradation.

---

# 69. Redis Failure Behavior

If Redis disappears:

```text
project API
```

should fall back to PostgreSQL.

Analytics ingestion could either:

```text
return temporary failure
```

or:

```text
use database fallback
```

depending on implementation.

Document the tradeoff.

---

# 70. Database Failure Behavior

If PostgreSQL is unavailable:

```text
health endpoint

→ DOWN
```

Read-only cached content may optionally still be served.

Do not over-engineer this initially.

---

# 71. CI Pipeline

Pull request:

```text
Checkout
   │
   ▼
Compile
   │
   ▼
Static Analysis
   │
   ▼
Unit Tests
   │
   ▼
Testcontainers Integration
   │
   ▼
Build Backend Image
   │
   ▼
Build Frontend
   │
   ▼
Start Complete Stack
   │
   ▼
API Tests
   │
   ▼
Contract Tests
   │
   ▼
Playwright
   │
   ▼
Performance Smoke
```

---

# 72. Static Analysis

Add:

```text
SpotBugs

Checkstyle

ESLint

TypeScript type checking
```

Optional:

```text
SonarCloud
```

---

# 73. Code Coverage

Generate coverage using:

```text
JaCoCo
```

Do not optimize purely for a high percentage.

Example threshold:

```text
80%
```

More important:

```text
business logic

failure paths

auth logic

rate limiting

cache behavior
```

must be covered.

---

# 74. Docker Compose Development Environment

Services:

```yaml
services:

  postgres:

  redis:

  backend:

  frontend:
```

Developer should be able to run:

```bash
docker compose up
```

and access:

```text
localhost:3000

localhost:8080
```

---

# 75. Environment Configuration

Use:

```text
application.yml

application-local.yml

application-test.yml

application-prod.yml
```

Secrets should come from environment variables.

Never commit:

```text
database passwords

GitHub tokens

JWT signing keys
```

---

# 76. Deployment Architecture

A practical first version:

```text
Vercel
   │
   ▼
Next.js

Backend Host
   │
   ▼
Spring Boot
   │
   ├── PostgreSQL
   │
   └── Redis
```

Possible backend providers:

```text
Render

Railway

Fly.io
```

Later deployment could move to:

```text
AWS ECS

Azure Container Apps

GCP Cloud Run
```

but this is not necessary for the first release.

---

# 77. Zero-Downtime Deployment

Optional advanced feature.

Deploy:

```text
Version A
```

then:

```text
Version B
```

verify health:

```text
/actuator/health
```

then redirect traffic.

This demonstrates deployment reliability.

---

# 78. Database Migration During Deployment

Run:

```text
Flyway migrations
```

before new application version becomes healthy.

Migrations should be backward-compatible where practical.

Example:

Instead of immediately:

```text
rename field
```

use:

```text
add new field

deploy code supporting both

migrate data

remove old field later
```

---

# 79. Security Requirements

At minimum:

```text
HTTPS

password hashing

JWT validation

role-based access

rate limiting

request validation

CORS configuration

SQL parameterization

secret management

secure headers
```

---

# 80. Password Storage

Use:

```text
Argon2

or

BCrypt
```

Never store plaintext passwords.

---

# 81. Input Security Tests

Automate tests for:

```text
SQL injection strings

XSS payloads

oversized JSON

malformed JSON

unexpected fields

path traversal strings
```

The purpose is defensive validation, not offensive security tooling.

---

# 82. Useful Engineering Invariants

Document these explicitly.

### Analytics

```text
one event ID
=
at most one stored event
```

### Cache

```text
cache may be stale temporarily

but must never contain structurally invalid data
```

### GitHub integration

```text
GitHub failure
must not crash project API
```

### Authentication

```text
revoked refresh token
must never create new access token
```

### Admin

```text
unauthenticated user
must never modify portfolio data
```

---

# 83. Recommended Development Phases

## Phase 1 — Backend Foundation

Implement:

```text
Spring Boot

PostgreSQL

Flyway

Projects API

Blog API

Docker Compose
```

Definition of done:

```text
GET /projects works

GET /posts works

data persists in PostgreSQL
```

---

# 84. Phase 2 — Testing Foundation

Add:

```text
JUnit

Testcontainers

REST Assured

GitHub Actions
```

Definition of done:

```text
PR automatically launches PostgreSQL

runs migrations

executes integration tests
```

---

# 85. Phase 3 — GitHub Integration

Implement:

```text
GitHub API client

background sync

retry

cached fallback
```

Test with WireMock.

This is one of the highest-value phases.

---

# 86. Phase 4 — Redis

Implement:

```text
API caching

cache invalidation

rate limiting
```

Add real Redis integration tests through Testcontainers.

---

# 87. Phase 5 — Analytics Pipeline

Implement:

```text
POST /events

Redis Stream

analytics worker

idempotency

daily aggregation
```

This creates the best backend systems component.

---

# 88. Phase 6 — Authentication/Admin

Implement:

```text
JWT

refresh tokens

admin API

project management
```

---

# 89. Phase 7 — Browser Automation

Add:

```text
Playwright

Chrome

Firefox

mobile viewport
```

Run it automatically in CI.

---

# 90. Phase 8 — Testing Platform

Create:

```text
testctl
```

for:

```text
environment startup

test-data creation

test orchestration

report generation
```

This makes the project particularly relevant for test-automation roles.

---

# 91. Phase 9 — Performance and Observability

Add:

```text
k6

Actuator

structured logs

application metrics
```

Optional:

```text
Prometheus

Grafana
```

---

# 92. Features to Prioritize

If time is limited, prioritize these in this order:

```text
1. Spring Boot REST API

2. PostgreSQL + Flyway

3. Testcontainers

4. REST Assured

5. GitHub synchronization worker

6. Retry/failure handling

7. Redis caching

8. GitHub Actions CI

9. Analytics event ingestion

10. Playwright
```

Then:

```text
11. Rate limiting

12. testctl

13. k6

14. OpenAPI contract testing

15. dashboards
```

---

# 93. Features Not Worth Prioritizing Early

Avoid spending significant time on:

```text
Kubernetes

microservices

Kafka

Elasticsearch

service mesh

complex cloud networking
```

They add architecture without giving enough additional value for the scope of this project.

A well-designed modular monolith is preferable.

---

# 94. Recommended Architecture Style

Use:

# Modular Monolith

Example:

```text
Spring Boot Application

├── Project Module
├── Blog Module
├── GitHub Module
├── Analytics Module
├── Auth Module
└── Contact Module
```

Not:

```text
7 independently deployed microservices
```

The project is too small to justify that operational complexity.

---

# 95. Flagship Engineering Features

If an interviewer only remembers five things, they should be:

### 1. Real deployed backend

```text
Spring Boot + PostgreSQL
```

### 2. Resilient GitHub integration

```text
retry + cache + fallback
```

### 3. Asynchronous analytics

```text
Redis Streams + idempotent worker
```

### 4. Production-style automated testing

```text
Testcontainers + REST Assured + Playwright
```

### 5. Fully automated CI pipeline

```text
GitHub Actions
```

---

# 96. Demonstration Scenario

A strong demo should deliberately introduce an error.

Example:

Change cache invalidation code so that updating a project does not clear Redis.

Push branch.

CI runs.

```text
Unit Tests

PASS

Integration Tests

FAIL
```

Report:

```text
ProjectCacheInvalidationTest

Expected:

"Updated project title"

Actual:

"Old project title"

Redis key:

project:navigation-assistant

Status:

STALE
```

Fix the bug.

Push.

```text
Integration

PASS

API

PASS

E2E

PASS
```

This demonstrates why your testing infrastructure exists.

---

# 97. Second Demo Scenario

Simulate GitHub outage.

WireMock returns:

```text
HTTP 500
```

Your test verifies:

```text
GET /projects
```

still returns:

```text
HTTP 200
```

with:

```text
last successfully cached GitHub metadata
```

This demonstrates graceful degradation.

---

# 98. Third Demo Scenario

Send duplicate analytics event:

```text
eventId = abc123
```

100 times.

Expected:

```text
Redis delivery count:

100
```

Database:

```text
1 event
```

Metric:

```text
PROJECT_VIEW +1
```

This demonstrates idempotency.

---

# 99. Performance Demo

Run:

```bash
k6 run portfolio.js
```

Show:

```text
100 virtual users

12,480 requests

p50:
21 ms

p95:
68 ms

p99:
117 ms

errors:
0.03%
```

Then demonstrate cache impact:

```text
Without Redis:

p95 184 ms

With Redis:

p95 68 ms
```

Now caching has measurable justification.

---

# 100. Final Resume Entry

**Personal Developer Platform — Production Backend & Test Infrastructure**
*Java, Spring Boot, PostgreSQL, Redis, Docker, Testcontainers, REST Assured, Playwright, GitHub Actions*

* Built and deployed a Spring Boot backend powering a personal portfolio with PostgreSQL persistence, Redis caching/rate limiting, full-text search, authenticated administration, and scheduled GitHub API synchronization with retry and cached fallback mechanisms.
* Designed an asynchronous analytics pipeline using Redis Streams and idempotent workers to ingest and aggregate portfolio events while maintaining deterministic processing under duplicate delivery and worker restarts.
* Developed automated unit, API, database, browser, contract, and performance testing infrastructure with JUnit, Testcontainers, REST Assured, Playwright, k6, and GitHub Actions, including deterministic test-data seeding and failure diagnostics.

---

# 101. Shorter Resume Version

If space is limited:

**Personal Developer Platform**
*Spring Boot, PostgreSQL, Redis, Docker, Testcontainers, Playwright*

* Built a deployed portfolio backend with REST APIs, PostgreSQL persistence, Redis caching/rate limiting, GitHub synchronization, and asynchronous event analytics with idempotent workers.
* Developed containerized API/integration/browser testing infrastructure using Testcontainers, REST Assured, Playwright, and GitHub Actions CI, with deterministic test-data seeding and performance regression checks.

---

# 102. Definition of Done

The project is portfolio-ready when:

```bash
git clone ...

docker compose up
```

starts:

```text
frontend

backend

PostgreSQL

Redis
```

and:

```bash
./testctl all
```

returns:

```text
Developer Platform Validation
────────────────────────────────

Unit                  PASS

Integration           PASS

PostgreSQL            PASS

Redis                  PASS

API                    PASS

Contract               PASS

Chrome E2E             PASS

Firefox E2E            PASS

Performance            PASS

────────────────────────────────

All checks passed
```

The live site should then demonstrate that the same backend and infrastructure being discussed on the resume are actively serving the portfolio itself.

The strongest framing of the project is not:

> "I made my personal website."

It is:

> "I built and operate a deployed backend platform with third-party integrations, asynchronous event processing, caching, reliability mechanisms, observability, and an automated testing system that validates the application across API, database, browser, and performance layers."
