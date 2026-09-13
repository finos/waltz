# Testing

How and when tests run in Waltz, and what is expected of contributions. This complements the
contribution rules in [`CONTRIBUTING.md`](CONTRIBUTING.md).

## When and how tests run

- **On every pull request and push:** the `maven-dual-build.yml` GitHub Actions workflow builds the
  multi-module Maven project and runs the automated test suite (Java integration tests and frontend
  `waltz-ng` mocha tests; mocha results are published as CI test output).
- **Locally:** contributors run the suite with the standard Maven build (`mvn verify` / the
  documented build command) before opening a pull request.
- **End-to-end:** a Playwright e2e suite (applications, technology, measurables, surveys,
  assessments, app-groups, flows, …) runs in CI via the `playwright-e2e` job; coverage continues to
  grow.

## Test expectations for changes

- **Bug fixes** SHOULD include a regression test that fails before the fix and passes after.
- **New features / significant behaviour changes** MUST add or update automated tests covering the
  new behaviour. A pull request that materially changes functionality without corresponding tests
  will be asked to add them before merge.
- **Refactors** MUST keep the existing suite green; add tests where the refactor exposes a gap.
- Documentation-only or trivial changes are exempt.

## Coverage & quality

- The project does not enforce a fixed numeric coverage gate today; reviewers assess whether the
  tests meaningfully exercise the change. Introducing coverage reporting is a candidate roadmap item.
- Tests MUST be deterministic; flaky tests should be fixed or quarantined with a tracking issue.

## Passing tests before merge

- CI MUST be green before a pull request is merged to `master`. Combined with the non-author review
  required by `CONTRIBUTING.md` and branch protection, this keeps `master` releasable.
