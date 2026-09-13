# Secrets Management & Credential Rotation Policy

> Project policy satisfying **OSPS-BR-07.02** (documented secrets/credential-rotation policy).

## Scope
This policy covers all secrets used by the Waltz project's build, test, release, and CI/CD
infrastructure. It does **not** cover secrets held by downstream deployers of Waltz — those are
the operator's responsibility (see the deployment documentation).

In-scope secrets include, but are not limited to:

- GitHub Actions organisation/repository secrets (e.g. `NVD_API_KEY`, `OSSINDEX_USER`,
  `OSSINDEX_TOKEN` used by CVE scanning; any publish/deploy credentials).
- Any package-registry, artifact-signing, or release-publishing credentials.
- Bot or automation tokens (e.g. dependency-update automation).

## Storage
- Secrets MUST be stored only in GitHub Actions encrypted secrets (repository or organisation
  scope) or an approved secrets manager. They MUST NOT be committed to the repository, embedded
  in workflow files, or printed to CI logs.
- Workflows MUST reference secrets via `${{ secrets.* }}` and MUST NOT echo them. Steps handling
  secrets SHOULD set `env:` at the narrowest scope needed.
- Secret scanning (GitHub secret scanning / push protection) SHOULD be enabled on the repository
  to catch accidental commits.

## Least privilege
- Each secret MUST be scoped to the smallest set of workflows/jobs that need it.
- Automation tokens MUST use the minimum permissions required (scoped `permissions:` blocks in
  workflows — OSPS-AC-04.02).

## Rotation
- Long-lived credentials MUST be rotated at least **every 12 months**.
- A secret MUST be rotated **immediately** on any of:
  - suspected or confirmed exposure (commit, log, screenshot, third-party leak);
  - departure of a maintainer who had access to it;
  - deprecation/compromise notice from the issuing provider.
- Prefer short-lived, automatically-issued credentials (e.g. OIDC-based federation) over
  long-lived tokens wherever the provider supports it, to reduce rotation burden.

## Ownership & review
- Each secret has a named maintainer owner responsible for its lifecycle.
- The maintainers review the inventory of active secrets at least **every 6 months**, removing
  any that are unused.

## Incident response
- On suspected exposure: revoke first, rotate, then assess blast radius. Report per the process
  in `SECURITY.md` (FINOS responsible disclosure) if third parties may be affected.

## Related
- [`../../../SECURITY.md`](../../../SECURITY.md) (vulnerability reporting) ·
  [`vulnerability-remediation.md`](vulnerability-remediation.md) ·
  [`../../../CONTRIBUTING.md`](../../../CONTRIBUTING.md)
