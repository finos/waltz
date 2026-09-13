# Waltz — Semi-Annual Report, 2026 H2

> FINOS TOC semi-annual health report for Waltz (Graduated). This copy is maintained in-repo for
> maintainer review; the report is submitted to `finos/technical-oversight-committee`
> (`project-reports/2026/2026-H2-Waltz.md`) ahead of the TOC review call.
> Metrics are **LFX Insights numbers of record** (insights.linuxfoundation.org, `finos/waltz`,
> past 365 days, read 2026-09-09) — the TOC reviews LFX data.

**Project Maintainers:** David Watkins (lead), Mark Guerriero, Shreyans Jain, Jessica
Woodland-Scott, Kuldeep Jindal, Mayank Gupta, and Meenakshi Saraf — all **Deutsche Bank**;
Rohit Vats; Kamran Saleem (**Think in Code**).
_(per `MAINTAINERS.md`; some name/org/email entries there are still being completed.)_

**Repository:** https://github.com/finos/waltz · Lifecycle stage: **Graduated** · License: Apache-2.0

# Project Overview

Waltz is an open-source tool for documenting and visualising an organisation's IT landscape —
applications, data flows, organisational units, people, technology (servers/databases),
capabilities/measurables, assessments and surveys. It gives enterprise architects a single,
queryable model of "what we have, how it connects, and how well it meets our standards",
aimed at regulated environments such as financial services. Java backend, AngularJS (`waltz-ng`)
frontend, PostgreSQL, multi-module Maven build.

# Current Status

- **LFX health score 92/100 ("Excellent")** — breakdown: Maintainer Health 40/40, Security &
  Supply Chain 31/35, Development Activity 21/25. Year-on-year, all headline development indicators
  are up (commits, pull requests, active contributors, stars, forks — see metrics below).
- **Lifecycle flagged "Declining" — addressed honestly.** LFX marks the lifecycle *Declining* on a
  single signal: maintainer activity has slowed over the last six months (9 active maintainers
  remain; median issue response ~1 day). This is the contributor-concentration risk we name openly
  below; the annual trend is growth, and our roadmap is weighted to broaden the maintainer base.
- **Sustained release cadence** — ~monthly releases through the period; latest **1.84.0 (2026-07-14)**;
  100 tagged releases to date; last release very recent.
- **Technology domain extended** — new create/ingestion endpoints for server and database information
  and usage (enables programmatic population of the technology model).
- **End-to-end test suite** — a Playwright e2e suite is now substantially built and under review:
  ~85 test cases across 12 functional areas (applications, technology, measurables, surveys,
  assessments, app-groups, flows and more), landing as one PR per area (#7604–#7612). It runs in CI
  via the `playwright-e2e` job, which passes — a strong, growing quality/regression signal.
- **Security scanning enabled (SCA + SAST)** — a combined SCA workflow (OWASP dependency-check for
  Java + auditjs for the frontend) was added report-only (PR #7597), and GitHub CodeQL SAST
  (Java + JavaScript) was enabled report-only (PR #7616). SCA surfaced real dependency debt, now
  tracked in #7595 (backend) and #7596 (frontend) — an honest security-health signal.

# Community & Contribution Metrics

_LFX Insights (`finos/waltz`), past 365 days, read 2026-09-09; deltas vs the prior 365-day period.
LFX now includes bot activity in development counts._

- **Active contributors:** **34** (up from 28; +21%) — 4 maintainers, 9 reviewers; spanning **6
  active organizations**.
- **Contributor concentration:** top 5 contributors account for **58%** of activity (lead ~21%);
  LFX characterises this as a *moderate spread*. Leading organisations by contribution: HMx Labs
  (50%), FINOS (40%), Think in Code (7%), Deutsche Bank (1%).
- **Commits:** **1,992** (up from 1,137; +75%, bot activity now counted).
- **Pull Requests:** **130 opened, 105 merged** (~120 closed); average merge velocity **5 days**,
  merge lead time ~4 days.
- **Issues:** **102 closed** in the window (flat vs prior period), ~27-day average resolution;
  standing open backlog ~370 (see Challenges).
- **Releases:** 100 total; latest 1.84.0 (2026-07-14); recent cadence ~monthly.
- **Popularity (in-window):** +39 new stars, +17 new forks; known adoption among financial-services
  institutions [add named adopters / case studies if shareable].

# Challenges & Blockers

- **Contributor concentration / bus factor** — this is the project's primary health risk and the
  reason LFX flags the lifecycle *Declining*: maintainer activity has slowed over the last six months.
  The distribution is a *moderate spread* (top author ~21%, top 5 = 58%, no single dominant author),
  and the lead-maintainer cadence has dipped over the period. Mitigation in progress: onboarding
  additional contributors, adding e2e tests to lower the change-risk barrier for new contributors,
  and documenting build/onboarding.
- **OSPS Baseline (Level 3) gaps** — as a Graduated project the target is OSPS Baseline **Maturity
  Level 3**. Governance/security docs are in place (`SECURITY.md`, `CONTRIBUTING.md`,
  `CODE_OF_CONDUCT.md`, `MAINTAINERS.md`), and build CI runs tests. Verified gaps to close this cycle:
  - **SCA and SAST enabled report-only** (SCA via PR #7597: dependency-check + auditjs; SAST via
    PR #7616: GitHub CodeQL); remaining work is to make them blocking once the standing backlog is
    burned down (OSPS-VM-05/06).
  - No release signing/provenance or verification instructions; no SBOM on releases (OSPS-DO-03, BR-02, QA-02.02).
  - Documented policies added and linked from `CONTRIBUTING.md` (secrets management, support/EOL
    per release, SCA/SAST remediation thresholds, testing); governance — roles, non-author review,
    maintainer qualification/voting — is already documented in `CONTRIBUTING.md`; threat model
    (OSPS-SA-03.02) in draft.
  - Branch protection is enabled on `master` with merge restricted to maintainers; the explicit
    "require non-author approval" rule is being confirmed to fully satisfy OSPS-QA-07.01.

  (See the [OSPS Baseline Level 3 checklist](osps-baseline-l3-checklist.md) for the full
  control-by-control status.)
- **Frontend modernisation** — `waltz-ng` runs on AngularJS 1.x (end-of-life). A staged migration
  plan is needed to manage long-term maintainability and security.
- **Open-issue backlog** — ~370 open issues; triage and grooming needed.

# Roadmap & Goals for Next 6 Months

- **Close OSPS Baseline Level 3 gaps** — branch protection with required review, release
  signing/provenance + SBOM, SCA/SAST in CI with documented policies, `SECURITY.md`/threat model,
  support & EOL policy. (Primary compliance goal.)
- **Integration & extensibility** — complete and document bulk ingestion/create APIs (apps, servers,
  databases, flows), publish an OpenAPI spec, and add idempotent external-id upsert.
- **Quality & CI** — merge the Playwright e2e suite (#7604–#7612, already running via the
  `playwright-e2e` job) and grow coverage; publish a documented test policy.
- **Platform modernisation** — publish a staged AngularJS migration plan and begin the first slice;
  dependency upgrades.
- **Community** — publish a public `ROADMAP.md`, broaden the maintainer base, and groom the issue backlog.

# TOC Support Needed

- **Lifecycle / compliance guidance** — concrete evidence expectations for OSPS Baseline Level 3 and
  how LFX Insights scores each control, so we can prioritise the highest-impact gaps.
- **Security tooling support** — pointers to FINOS/OpenSSF resources for release signing/provenance,
  SBOM generation, and SCA/SAST setup.
- **Community growth & marketing** — help broadening the contributor/adopter base and surfacing
  named adopters, to reduce contribution concentration.

# Additional Information

- Multi-module Maven project; Java backend + AngularJS frontend + PostgreSQL.
- Upstream: https://github.com/finos/waltz
