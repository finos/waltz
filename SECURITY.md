# Waltz Security Policies and Procedures

This document outlines security procedures and general policies for the
Waltz Open Source project as found on https://github.com/finos/waltz.

  * [Responsible Disclosure](#responsible-disclosure)
  * [Reporting a Vulnerability](#reporting-a-vulnerability)
  * [Disclosure Policy](#disclosure-policy)

## Responsible Disclosure

As a FINOS project Waltz falls under the standard [FINOS Security Vulnerabilities Responsible Disclosure Policy](https://community.finos.org/docs/governance/software-projects/cve-responsible-disclosure/).


## Reporting a Vulnerability 

The Waltz OSS team and community take all security vulnerabilities
seriously. Thank you for improving the security of our open source 
software. We appreciate your efforts and responsible disclosure and will
make every effort to acknowledge your contributions.

For more information on reporting a vulnerability see: [Submit a new security vulnerability](https://community.finos.org/docs/governance/software-projects/cve-responsible-disclosure/#submit-a-new-security-vulnerability)

## Disclosure Policy

When the security team receives a security bug report, they will assign it
to a primary handler. This person will coordinate the fix and release
process, involving the following steps:

  * Confirm the problem and determine the affected versions.
  * Audit code to find any potential similar problems.
  * Prepare fixes for all releases still under maintenance. These fixes
    will be released as fast as possible.
  
## Known Vulnerabilities

Known vulnerabilities in Waltz's dependencies are tracked as GitHub issues — currently the backend
([#7595](https://github.com/finos/waltz/issues/7595)) and frontend/AngularJS
([#7596](https://github.com/finos/waltz/issues/7596)) dependency debt — and are surfaced by the
project's dependency/security scanning. See the
[vulnerability remediation policy](docs/governance/policies/vulnerability-remediation.md) for how
findings are triaged and remediated, and the repository **Security** tab for advisories.
