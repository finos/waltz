# Direct relationships

## Status: DRAFT

## Motivation
Entities in Waltz can be linked together either directly (in the case of apps to org units) or via another linking entity (org units to measurables via application ratings). 


## Proposal

Currently have an individual endpoint for fetching directly related measurables for an org unit. From a person view, related applications have both direct relationships and inherited relationships expressed. We want to have direct involvements and inherited involvements from the person view. 

This could make use of the IdSelectorFactory, however, the factory would need to be able to tell from the selector whether the direct or indirect relationship is required. 

This could be passed in on the selector object, as:

const selector = {
    entityReference: ref,
    scope: "EXACT,
    linkingEntity: null 
} 

If linkingEntity is null then expect a direct relationship, if an entityKind is provided then use this as the bridging entity. 

1) Could have two separate selector factories for direct selector/ indirect selector and service decided between them.

2) Have it handled within the IdSelectorFactory method, to do a direct/indirect query.


This is different from inheriting associations from children.


##Impact


14 classes implementing IdSelectorFactory as well as ApplicationIdSelectorFactory

Everywhere an idSelectorFactory is used (~84 uses??) will need to read direct/indirect from the options
Everywhere options are constructed (~ 71 uses??) must provide a linking entity type (if default is to be null in selector-utils).
Could have default behaviour within a method in IdSelectorFactory?     
     
mkSelectionOptions() to set a default in js.


Example of which methods in MeasurableIdSelectorFactory are currenlty 'direct' or 'indirect'

PERSON: Indirect via Apps

MEASURABLE_CATEGORY: Direct

MEASURABLE: Direct

APP_GROUP: Indirect via App

FLOW_DIAGRAM: Unions Direct and Indirect? 

SCENARIO: Direct

ORG_UNIT: Indirect via Apps

ACTOR / APPLICATION:
Direct / via Measurable Ratings/Replacements
