# Waltz e2e (Playwright, JS)

End-to-end browser tests for Waltz. Plain JavaScript (ESM) — matching the `waltz-ng` frontend
conventions (no TypeScript in this repo). Test data is seeded via **Waltz's REST API** (the same
endpoints the UI uses), not via in-process Java helpers.

## Running

### Ephemeral (self-contained — what CI runs)

Spins up throwaway Postgres + Waltz app containers via Testcontainers, seeds sample data with
`LoadAll`, runs the suite against the app's mapped port, and tears everything down. Nothing on the
host is assumed beyond Docker + the built artifacts.

```bash
npm install
npx playwright install chromium
npm run test:e2e                      # whole suite, ephemeral backend
npm run test:e2e -- tests/bookmarks.spec.js   # one spec (args passed through)
```

Prerequisites (build artifacts): the Waltz Docker image and the jobs uber-jar.
- Image via `E2E_WALTZ_IMAGE` (default `waltz-dev-waltz:latest`; CI builds `waltz-e2e:ci`).
- Jobs jar auto-resolved from `../waltz-jobs/target/uber-waltz-jobs-*.jar` (build with
  `mvn -pl waltz-jobs -am package -P waltz-postgres,dev-postgres -DskipTests`), or set `E2E_JOBS_JAR`.
- `E2E_SKIP_LOADALL=1` skips sample-data seeding (faster for specs that self-seed).

### Against an already-running instance (fast local iteration)

```bash
WALTZ_BASE_URL=http://localhost:8080 npx playwright test               # whole suite
WALTZ_BASE_URL=http://localhost:8080 npx playwright test tests/bookmarks.spec.js
```

`WALTZ_BASE_URL` defaults to `http://localhost:8080`. Use this when you already have a populated
Waltz running; the ephemeral runner above sets `WALTZ_BASE_URL` for you.

## Report + screenshots

After every run, an **HTML report with screenshots, traces and video-on-failure** is written to
`playwright-report/`. Open it with:

```bash
npx playwright show-report
```

Config: `screenshot: "on"`, `trace: "on"` (see `playwright.config.js`).

## Authoring pattern

```js
import { test, expect } from "@playwright/test";
import { authenticate, createApp, login, apiContext, uniqueName } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

test("does a thing", async ({ page, context }) => {
    const token = await login(baseURL);              // admin / password -> JWT
    const app = await createApp(baseURL, token, uniqueName("myapp"));  // seed via API

    await authenticate(context, token);              // UI reads JWT from localStorage
    await page.goto(`/application/${app.id}`);
    // ... drive UI, assert ...
});
```

`helpers/api.js` provides: `login`, `apiContext` (bearer-authed APIRequestContext), `createApp`,
`authenticate` (seeds `localStorage["satellizer_token"]`), `uniqueName`.

## Rules for new specs

1. **Seed via the REST API**, not the UI, for setup. Use `apiContext(baseURL, token)` and POST to the
   relevant endpoint. Discover payloads by reading `waltz-web/.../endpoints/api/*Endpoint.java` and the
   Java helpers in `waltz-test-common/src/main/java/org/finos/waltz/test_common/helpers/*`.
2. **Use unique names** (`uniqueName(prefix)`) so parallel/repeat runs don't collide.
3. **Do NOT hardcode 2023 sample-data names.** The old Java tests reference entities like "Book Data",
   "CEO Office", "Information Classification" that no longer exist. Either seed what you need via API, or
   query existing baseline data via API (e.g. `GET /api/involvement-kind`, `GET /api/assessment-definition`)
   and pick one dynamically.
4. **Locators**: prefer `getByTestId(...)` (the app uses `data-testid`). For other selectors, consult the
   current `waltz-ng/client/**` source — section names and classes have changed since 2023 (e.g. the
   measurable section is now "Ratings / Roadmaps", plural).
5. The old Java tests in `waltz-test-common/src/test/java/org/finos/waltz/test_common/playwright/` are the
   reference for **flows**, but their locators and data names are bitrotted — treat them as a guide, not gospel.
6. **Don't edit** `helpers/api.js`, `playwright.config.js`, or other people's spec files. Put area-specific
   helpers in your own spec file or a new `helpers/<area>.js`.
7. If a test needs data that has **no create endpoint**, mark it `test.fixme(...)` with a comment naming the
   gap (see below) rather than faking it.

## Known API endpoints (verified)

- `POST /authentication/login` `{userName, password}` → `{token}` (admin / password)
- `POST /api/app` → create application (see `createApp`)
- `GET  /api/app/id/:id`, `GET /api/app/search/:query`
- `GET  /api/involvement-kind` (14 baseline kinds incl. "IT Architect")
- `GET  /api/assessment-definition` (8 baseline defs incl. "Sensitive Data")
- `GET  /api/org-unit/all` (baseline org units; id 10 works for `createApp`)
- App groups: `POST /api/app-group/*` (add-owner, add-application, application-list, etc. — see `AppGroupEndpoint.java`)
- `PUT /api/assessment-definition` create/update a definition (needs `ADMIN`/`ASSESSMENT_DEFINITION_ADMIN`; body must include `lastUpdatedBy`/`lastUpdatedAt`). Seed a `isReadOnly:false` def to allow rating — baseline defs are read-only.
- `POST /api/user/:userName/roles` grant roles additively (union with `GET /api/user/whoami`) then re-login; needed for write ops (admin starts with only `ADMIN`).

## Known seeding gaps (no create REST endpoint — confirmed)

These are created direct-to-DB by the Java helpers because there's no API:
- **Rating schemes** (no create endpoint)
- **Persons** (feed-loaded; no create endpoint)
- **Databases** (`DatabaseInformationEndpoint` — no create)

Tests that need these as setup (e.g. report-grid's custom assessment, surveys' recipient person) can:
- reuse a **baseline** definition/kind queried via API, or
- be marked `test.fixme` with the gap noted, pending a seed step (LoadAll / test-only endpoint).
