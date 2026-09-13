# Support & End-of-Life Policy

> Project policy satisfying **OSPS-DO-04.01** (statement of support scope/duration per release) and
> **OSPS-DO-05.01** (statement of when security updates end per release).
> The specific support windows below are proposed for maintainer confirmation.

## Release model
Waltz follows a rolling release model with `MAJOR.MINOR.PATCH` version numbers and an
approximately monthly cadence (latest: 1.84.0, 2026-07-14; 100 releases to date). Releases are
published as jar/war/zip assets under the corresponding Git tag.

## Support scope
- **Actively supported:** the **latest released version** is the supported baseline. Bug reports,
  security fixes, and questions are handled against the latest release and `master`.
- Users are expected to upgrade forward to receive fixes; Waltz does not maintain long-lived
  parallel maintenance branches for older minor versions.
- "Support" here means community best-effort via GitHub issues and the FINOS channels — there is
  no commercial SLA from the project itself.

## Security-update duration
- Security fixes are delivered in the **next release on `master`**, i.e. against the latest line.
- Older releases do **not** receive back-ported security patches; the supported remediation is to
  upgrade to the latest release.
- Security issues are reported and handled per `SECURITY.md` (FINOS responsible disclosure).

## End-of-life
- A release reaches practical end-of-life when a newer release supersedes it, because fixes land
  forward on `master` rather than being back-ported.
- Any deviation (e.g. a designated long-term-support line, should one ever be created) will be
  documented explicitly in the release notes and here.

## Platform/runtime support
- Supported Java, database (PostgreSQL / others), and browser baselines are documented in the
  project README / deployment docs; those baselines move forward with releases.
- The AngularJS 1.x frontend is a known end-of-life dependency; its migration is tracked on the
  public roadmap (see #7596 for the associated security exposure).

## Related
- [`../../../SECURITY.md`](../../../SECURITY.md) ·
  [`vulnerability-remediation.md`](vulnerability-remediation.md) ·
  [`../../../ROADMAP.md`](../../../ROADMAP.md)
