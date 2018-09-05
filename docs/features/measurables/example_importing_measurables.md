# Example: Importing Measurables

## Overview

This article will demonstrate how to import a measurable taxonomy into Waltz using a straightforward 
Java program and should be easily adaptable to other technology approaches.

This example will use a taxonomy published by the American Productivity & Quality 
Center [(APQC)](https://www.apqc.org/).  This site contains numerous _Process Classification Frameworks_, 
for this example we will download and use the [Banking PCF](https://www.apqc.org/knowledge-base/documents/apqc-process-classification-framework-pcf-banking-pcf-excel-version-705).

## Objectives

- PCF Spreadsheet
    - Analyse file to determine mapping requirements
    - Parse and extract relevant fields into domain objects
- Waltz
    - Setup database access layer
    - Convert domain objects to measurable records
    - Create Waltz category
    - Insert measurable records 
    - Update parent ids
    - Modify importer to handle updates/deletions/creations
    - Run the hierarchy rebuild process
    
## PCF Spreadsheet

### Analysis

Looking at the spreadsheet we see numerous sheets, we will be primarily working 
with:

- _Combined_
- _Glossary terms_

The _Combined_ sheet contains all the taxonomy items which me can map to Waltz's `measurable` table:

| Name | Description | Analysis |
| --- | --- | --- |
| `PCF ID` | identifier for item | Map to Waltz `external_id` column |
| `Hierarchy Id` | dotted hierarchy notation (e.g. `1.2.3.1`) | We will need to process these to work out `external_parent_id` values |
| `Name` | Name of taxonomy item | Map to `name` column|
| `Difference Index` | Not required | - |
| `Change Details` | Not required | - |
| `Metrics Avialable ?` | Not required | - |

We can see that there are approximately 1800 items in this hierarchy reaching a depth 
of 5 (e.g. `13.2.3.3.5`).  With Waltz we have found that a slightly coarser grained model
works better so we will limit our import to a depth of 3.
   
The _Glossary terms_ sheet contains descriptions keyed against the `PCF ID` and 
the `Hierarchy Id`.  We will simply load them into a map and use them to enrich the 
taxonomy elements via the `decription` column in the `measurable` table.

