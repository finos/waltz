# OSPS Baseline — Level 3 Gap Checklist for Waltz (Graduated)

> Source: OpenSSF OSPS Baseline (`github.com/ossf/security-baseline`, `baseline/OSPS-*.yaml`).
> Graduated projects target **Maturity Level 3**: 63 of 65 controls apply, of which **21 are
> Level-3-specific** (listed below). The TOC assesses compliance via LFX Insights.
> Statuses below were verified against `finos/waltz` on 2026-09-04 (audit of repo contents,
> workflows, release assets). Branch protection on `master` is enabled with merge restricted to
> maintainers (confirmed by maintainers 2026-09-09).
> Updated 2026-09-06 for PR #7597 (SCA re-enabled, report-only);
> 2026-09-09 for the Playwright e2e suite now running in CI (#7604–#7612), CodeQL SAST enabled
> report-only (#7616), and branch protection confirmed.

## Verified repo audit — headline findings

- ✅ Present: `LICENSE` (Apache-2.0), `SECURITY.md` (FINOS responsible disclosure), `CONTRIBUTING.md`,
  `CODE_OF_CONDUCT.md`, `MAINTAINERS.md` (9 maintainers; some placeholder entries), `CONTRIBUTORS.md`.
- ✅ Build CI active (`maven-dual-build.yml`) runs Maven integration tests + publishes mocha test results.
- ⚠️ **SCA re-enabled (report-only) via PR #7597** (open, mergeable): one workflow runs OWASP
  dependency-check (Java) + auditjs (frontend), single combined `cve-reports` artifact. Not yet
  blocking; findings tracked in #7595 (backend) / #7596 (frontend).
- ⚠️ **SAST (GitHub CodeQL) enabled (report-only) via PR #7616**: Java + JavaScript, findings in the
  Security tab + inline PR annotations. Not yet blocking. Making SCA + SAST blocking (once the
  backlog is burned down) = remaining fast wins.
- ❌ No `dependabot.yml`. ❌ Releases unsigned, no SBOM (1.84.0 = plain jar/war/zip).
- ✅ Branch protection enabled on `master`; merge rights restricted to maintainers, changes land via
  reviewed PRs (confirmed by maintainers 2026-09-09). Confirm the explicit "require non-author
  approval" rule to fully satisfy OSPS-QA-07.01.

## Level-3-specific requirements (the Graduated-only bar)

### Access Control
- [ ] **OSPS-AC-04.02** — CI/CD jobs use minimum privileges. ❓ _Assess GitHub Actions `permissions:` blocks._

### Build & Release
- [ ] **OSPS-BR-01.04** — CI sanitises trusted-collaborator input. ❓ _Assess._
- [ ] **OSPS-BR-02.02** — Release assets associated with a release/unique id. ⚠️ _Assets exist under tag; no provenance._
- [x] **OSPS-BR-07.02** — Documented secrets/credential-rotation policy. ✅ _Documented (this PR): `policies/secrets-management.md`._

### Documentation
- [ ] **OSPS-DO-03.01** — Instructions to verify release integrity/authenticity. ❌ _Gap — needs signing first._
- [ ] **OSPS-DO-03.02** — Instructions to verify author/process identity. ❌ _Gap._
- [x] **OSPS-DO-04.01** — Statement of support scope/duration per release. ✅ _Documented (this PR): `policies/support-and-eol.md`._
- [x] **OSPS-DO-05.01** — Statement of security-update end per release. ✅ _Documented (this PR): `policies/support-and-eol.md`._

### Governance
- [x] **OSPS-GV-04.01** — Policy: collaborators reviewed before escalated permissions. ✅ _Documented: `CONTRIBUTING.md` (Maintainer Qualifications & Voting)._

### Quality
- [ ] **OSPS-QA-02.02** — SBOM delivered with release assets. ❌ _Gap — no SBOM on 1.84.0._
- [ ] **OSPS-QA-04.02** — Multi-repo releases: subprojects ≥ primary security. ➖ _Likely n/a (single repo)._
- [x] **OSPS-QA-06.02** — Docs state when/how tests run. ✅ _Documented (this PR): `TESTING.md`;
  Maven ITs + mocha in build CI, Playwright e2e via `playwright-e2e` (#7604–#7612)._
- [x] **OSPS-QA-06.03** — Policy that major changes add/update tests. ✅ _Documented (this PR): `TESTING.md`._
- [ ] **OSPS-QA-07.01** — ≥1 non-author approval before merge to `master`. ⚠️ _Documented in
  `CONTRIBUTING.md` (non-author review) + branch protection enabled, merge restricted to maintainers
  (2026-09-09). Confirm the "require non-author approval" rule is enforced to fully satisfy._

### Security Assessment
- [ ] **OSPS-SA-03.02** — Threat model / attack-surface analysis. ❌ _Gap._

### Vulnerability Management
- [ ] **OSPS-VM-04.02** — VEX for non-affecting component vulns. ❌ _Gap._
- [x] **OSPS-VM-05.01** — Policy: remediation threshold for SCA findings. ✅ _Documented (this PR): `policies/vulnerability-remediation.md`._
- [x] **OSPS-VM-05.02** — Policy: address SCA violations before release. ✅ _Documented (this PR): `policies/vulnerability-remediation.md` (release gating)._
- [ ] **OSPS-VM-05.03** — Automated dependency vuln/malicious-dep eval, blocking. ⚠️ _SCA now runs report-only (PR #7597); make blocking to satisfy._
- [x] **OSPS-VM-06.01** — Policy: remediation threshold for SAST findings. ✅ _Documented (this PR): `policies/vulnerability-remediation.md`._
- [ ] **OSPS-VM-06.02** — Automated SAST eval, blocking (with suppression). ⚠️ _CodeQL enabled report-only via #7616; make blocking to satisfy._

## Inherited (Levels 1–2) — status from audit
- ✅ `LICENSE` Apache-2.0; ✅ `SECURITY.md` reporting process; ✅ `CONTRIBUTING`/`CODE_OF_CONDUCT`;
  ✅ `MAINTAINERS.md` roles (orgs filled this PR; one org still TBC — OSPS-GV-01); ✅ CI runs on changes.
- ❓ Confirm: org-wide 2FA, least-privilege access, dependency update mechanism (no Dependabot found).

## Suggested sequencing (highest impact first)

1. **Enforce + extend scanning.** SCA (PR #7597) and CodeQL SAST (PR #7616) now run report-only;
   remaining fast wins are to make them blocking (once the backlog is burned down) and add
   `dependabot.yml` (OSPS-VM-05/06).
2. **Branch protection**: enabled, merge restricted to maintainers; confirm the explicit
   "require non-author review" rule on `master` (OSPS-QA-07) — also cuts bus-factor risk.
3. **Policies as docs** — done this PR: secrets, support/EOL, SCA/SAST remediation thresholds
   (`docs/governance/policies/`), testing (`TESTING.md`); governance/review already in `CONTRIBUTING.md`.
4. **Release integrity**: sign releases + provenance + verification instructions + SBOM generation.
5. **Threat model**: attack-surface analysis document (OSPS-SA-03.02).

> With the policy docs landed, the main remaining Level-3 gaps are **release integrity**
> (signing/provenance/SBOM) and the **threat model**.
