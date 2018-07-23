# Surveys

## Overview

Surveys allow for the collection of ad-hoc data within 
the overall structure of Waltz.  Surveys are focused on a 
specific entity type (e.g. APPLICATION, CHANGE_INITIATIVE) 
and issued to users matching a given set of roles describing 
their involvement with the entity.  The set of target entities
for the survey is derived from a Waltz selection mechanism 
(e.g. apps under a specific organisational unit, apps performing
a specific function etc.).

![Screenshot 1](images/survey_screenshot.png)

![Screenshot 2](images/survey_edit_screenshot.png)

## Model

![Schema Diagram](images/survey_schema.png)
[Source](https://app.quickdatabasediagrams.com/#/schema/YLytE3nJy0OVTId-YZkXew)

## Templates

Survey templates are collection of survey questions for a specific target entity kind.
Survey runs are created based on a survey template.

#### State Diagram

![Template State Diagram](http://www.gravizo.com/g?@startuml;[*]%20--%3E%20DRAFT;DRAFT:%20can%20be%20edited;DRAFT%20--%3E%20ACTIVE;ACTIVE%20:%20cannot%20be%20edited;ACTIVE%20:%20can%20be%20used%20to%20create%20runs;ACTIVE%20--%3E%20OBSOLETE;OBSOLETE%20:%20cannot%20be%20edited;OBSOLETE:%20cannot%20be%20used%20to%20create%20runs;OBSOLETE%20--%3E%20ACTIVE;@enduml;)


---
[Back to ToC](../README.md)