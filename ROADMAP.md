# Waltz Roadmap

> Themes are ordered to (a) sustain the project's FINOS Graduated health signals and (b) reach
> OSPS Baseline Maturity Level 3. Timeframes are indicative and reviewed each half-year.

## Theme 1 — Security & Compliance (OSPS Baseline Level 3)

Target: satisfy OSPS Baseline Maturity Level 3 (the Graduated bar).

- Branch protection: require at least one non-author human review before merge (OSPS-QA-07).
- Release integrity: sign releases, publish provenance, and document verification steps (OSPS-DO-03, OSPS-BR-02).
- Software Bill of Materials (SBOM) attached to releases (OSPS-QA-02.02).
- Automated SCA + SAST in CI with documented remediation thresholds and blocking policy (OSPS-VM-05, OSPS-VM-06).
- `SECURITY.md`, threat model / attack-surface analysis (OSPS-SA-03.02), secrets-management policy (OSPS-BR-07.02).
- Support & EOL policy per release (OSPS-DO-04, OSPS-DO-05).

## Theme 2 — Integration & Extensibility

- **Plugin / module architecture** — explore a plugin model letting adopters extend Waltz over a stable core.
- **MCP server** — explore an AI-agent interface to the Waltz model, aligned with the FINOS [AI Governance Framework](https://github.com/finos/ai-governance-framework).
- **FINOS ecosystem interoperability** — explore integrations with complementary projects (e.g. [CALM](https://github.com/finos/architecture-as-code), [CCC](https://github.com/finos/common-cloud-controls), [AIGF](https://github.com/finos/ai-governance-framework)) to aid adoption.
- Complete and document bulk ingestion / create APIs (applications, servers, databases, flows).
- Publish an OpenAPI / Swagger specification for the REST surface.
- Idempotent upsert keyed by external id (imports update rather than duplicate).
- Extension points / webhooks for downstream synchronisation.

## Theme 3 — Quality & CI

- Grow the Playwright end-to-end suite (now running in CI) across core modules.
- Publish a documented test policy: when/how tests run, and the expectation that changes add/update tests (OSPS-QA-06).
- Release automation and a published, predictable release cadence.

## Theme 4 — Platform Modernisation

- Publish a staged migration plan away from AngularJS 1.x (end-of-life) for `waltz-ng`.
- Deliver the first migration slice; ongoing Java/dependency upgrades.

## Theme 5 — Documentation & Onboarding

- Deployment guides, API documentation, getting-started, and data-model documentation.
- Governance/maintainers documentation (roles and responsibilities).

## Theme 6 — End-user Features

- Reporting / report-grid enhancements.
- Assessment and survey UX improvements.
- Performance improvements on large landscapes.

## Cadence & Governance

- Regular releases on a published cadence.
- Public roadmap (this document), reviewed each half.
- Semi-annual FINOS TOC health report and presentation.
