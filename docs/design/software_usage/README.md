# Software Usage

## Status: Draft

## Motivation
Waltz currently holds details of applications but not about the application makeup, in terms of the software 
packages / libraries it is composed of.  The purpose of this feature is to provide a view on an application's use of
third party software packages i.e. Log4j, Spring, Apache Commons etc.  This will also provide a view of the risk 
exposure of an application with regards to Software Vulnerabilities and Licence exposure as a result of the direct 
(or indirect) use of a third party library.

## Use cases / Proposal
- To be able to view the the third party libraries an application is using and drill down into the specific versions
- To be able to identify which licences an applications is exposed to and thereby identify any compliance conditions 
that would need to be adhered to
- To be able to identify if an application is susceptible to any Software Vulnerabilities as a consequence of it's use
of third party libraries
- To be able to identify the scope and pervasiveness of a software package within the organisation and then to be 
able to drill down to view the version makeup
- Similar to the above for licences
- An aggregate view of Software Risk based on the vulnerabilities identified in applications across the organisation


## Implementation

### Software Package
A represenation of a software package / library

Name            |       Type            | Description
---             | ---                   | ---
Id              | Long                  | Sequential identifier
Vendor          | String                | A group or namespace for this package, could double up as the publisher
Name            | String                | Name of the package
Description     | String                | Description
External Id     | String                | External identifier based on source of package
Created At      | Timestamp             | -
Created By      | User Id               | -
Provenance      | String                | Where this data point came from


### Software Package Version
A listing of the licences for the software version. 

Name                 |       Type            | Description
---                  | ---                   | ---
Id                   | Long                  | Sequential identifier
Software Package Id  | Long                  | FK to Software Package
Version?             | String                | Version of the package (nullable to represent the unknown)
Description          | String                | Description
External Id          | String                | External identifier
Created At           | Timestamp             | -
Created By           | User Id               | -
Provenance           | String                | Where this data point came from


### Software Version Usage
An application's usage of a software package is version specific, hence will link to this record.

Name                 |       Type            | Description
---                  | ---                   | ---
Id                   | Long                  | Sequential identifier
Application Id       | Long                  | FK to Application
Software Version Id  | Long                  | FK to Software Package version (a usage will always be for a specific version)
Created At           | Timestamp             | -
Created By           | User Id               | -
Provenance           | String                | Where this data point came from


### Software Version Licence
A software package usually has a version, 

Name                 |       Type            | Description
---                  | ---                   | ---
Id                   | Long                  | Sequential identifier
Software Version Id  | Long                  | FK to Software Package version (a licence will always be for a specific version)
Licence Id           | Long                  | FK to Licence
Created At           | Timestamp             | -
Created By           | User Id               | -
Provenance           | String                | Where this data point came from