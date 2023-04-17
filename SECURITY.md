# Atomist Open Source Security Policies and Procedures

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
  
## Current Known Vulvnerabilities

Below is a list of vulnerabilites we are aware of.  
Each vulnerability is given a Waltz rating based upon how we _know_ the code is used within the Waltz database.

| Vulnerability | Java/JS | Waltz Severity | Status |
| --- | --- | --- | --- | 
| h2  | Java | Low | No fix currently available (2023-Q1).  Not severe as only used for in-memory integration testing |
| sparkframework | Java | Medium | Fix available but breaks Tomcat deployment (2023-Q1) | 
