# Waltz - Semi-Annual Report, 2026 H2

> FINOS TOC semi-annual health report for Waltz (Graduated). This copy is maintained in-repo
> for maintainer review; it is submitted to `finos/technical-oversight-committee`
> (`project-reports/2026/2026-H2-Waltz.md`) ahead of the TOC review call.
>
> Development metrics are from the GitHub API (`finos/waltz`, trailing 365 days, as of
> 2026-10-01). Health and maintainer metrics are from LFX Insights.

**Project Maintainers:** David Watkins (lead), Mark Guerriero, Shreyans Jain, Jessica
Woodland-Scott, Kuldeep Jindal, Mayank Gupta, Meenakshi Saraf (all **Deutsche Bank**); Rohit
Vats; Kamran Saleem (**Think in Code**). Per `MAINTAINERS.md`.

**Repository:** https://github.com/finos/waltz · Lifecycle: **Graduated** · License: Apache-2.0

# Project Overview

Waltz documents and visualises an organisation's IT landscape: applications, data flows,
organisational units, people, technology (servers and databases), capabilities, assessments and
surveys. It gives enterprise architects one queryable model of what they have, how it connects,
and how well it meets their standards. It targets regulated environments such as financial
services. It has a Java backend, an AngularJS (`waltz-ng`) frontend, a PostgreSQL store, and a
multi-module Maven build.

# Current Status

- **Security scanning covers SCA and SAST.** OSV-Scanner scans the Maven and npm dependencies on
  every push and reports to the GitHub Security tab (#7628). OWASP dependency-check runs weekly as
  a cross-check (#7597, #7625). GitHub CodeQL runs SAST (#7616). The scans found real dependency
  debt, tracked in #7595 (backend) and #7596 (frontend).
- **End-to-end tests merged.** A Playwright suite of about 85 tests across 12 functional areas is
  now merged (#7604 to #7612; flaky cases fixed in #7630). It runs in CI and must pass before merge.
- **Technology model extended.** New endpoints create and ingest server and database information,
  which lets teams populate the technology model programmatically.
- **Governance documentation drafted.** Governance, security, and roadmap documentation for the TOC
  review is in review (#7617), including project policies and a public `ROADMAP.md`.
- **Release cadence has slowed.** The latest release is 1.84.0 (2026-07-14), about 11 weeks ago.
  The project has over 100 releases in total. Earlier cadence was about monthly.

# Community & Contribution Metrics

_GitHub API (`finos/waltz`), trailing 365 days, as of 2026-10-01:_

- **Pull requests:** 138 opened, 120 merged.
- **Issues:** 113 closed; 380 open (standing backlog).
- **Popularity:** 239 stars, 146 forks.
- **Releases:** over 100 total; latest 1.84.0 (2026-07-14).

_[LFX Insights](https://insights.linuxfoundation.org/project/waltz):_

- **Health score: 72/100 (Healthy).** Maintainer Health 22/40, Security and Supply Chain 31/35,
  Development Activity 19/25.
- **Maintainers:** 9 active maintainers with merge rights; median issue response about 1 day.
- **Organisations:** contributors span about 12 organisations, but a few top contributors make most
  of the changes (see Challenges).

# Challenges & Blockers

- **Contributor concentration and bus factor.** This is the main health risk. A few contributors
  make most changes, and maintainer activity has slowed over the last six months. This holds down
  the LFX Maintainer Health score. Mitigation: onboard more contributors, use the new e2e tests to
  lower the change risk for newcomers, and document the build and onboarding.
- **OSPS Baseline (Level 3) gaps.** The target is OSPS Baseline Maturity Level 3. Governance and
  security documentation is in place or in review (#7617). Remaining gaps:
  - Make SCA and SAST blocking once the current backlog is cleared (OSPS-VM-05, VM-06).
  - Attach a signed SBOM to releases. A build-time CycloneDX SBOM already exists for scanning
    (#7628); release signing and provenance are still to do (OSPS-DO-03, BR-02, QA-02.02).
  - Finish the threat model (OSPS-SA-03.02).

  (See the [OSPS Baseline Level 3 checklist](https://github.com/finos/waltz/blob/master/docs/governance/osps-baseline-l3-checklist.md).)
- **Frontend modernisation.** `waltz-ng` runs on AngularJS 1.x (end-of-life). It needs a staged
  migration plan.
- **Open-issue backlog.** About 380 open issues need triage and grooming.

# Roadmap & Goals for Next 6 Months

A public `ROADMAP.md` is in review (#7617). The priorities for the next six months are:

- **Close OSPS Baseline Level 3 gaps:** required non-author review, release signing and provenance
  plus SBOM, and a threat model.
- **Reduce the dependency-vulnerability backlog** (#7595, #7596) and make SCA and SAST blocking in
  CI.
- **Broaden the maintainer base** and groom the issue backlog, to reduce contributor concentration.
- **Explore a plugin and module architecture** so adopters can extend Waltz over a stable core. This includes AI and LLM integration and an MCP server that exposes the Waltz model to AI agents, aligned with the FINOS AI Governance Framework.
- **Integration and extensibility:** finish and document the bulk ingestion and create APIs,
  publish an OpenAPI spec, and add idempotent external-id upsert.
- **Platform modernisation:** publish a staged AngularJS migration plan and start the first slice.
- **Quality:** grow coverage on the merged Playwright suite and publish a documented test policy.

# TOC Support Needed

- **Compliance guidance:** the evidence the TOC expects for OSPS Baseline Level 3, and how LFX
  scores each control, so the project can prioritise the highest-impact gaps.
- **Security tooling:** FINOS and OpenSSF pointers for release signing, provenance, and SBOM.
- **Community growth:** help to broaden the contributor and adopter base and to surface named
  adopters.

# Additional Information

- Multi-module Maven project: Java backend, AngularJS frontend, PostgreSQL.
- Upstream: https://github.com/finos/waltz
