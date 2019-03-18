# Roadmaps and Scenarios

## Overview 

Roadmaps and Scenarios are two closely related concepts in Waltz. Conceptually a _Roadmap_ is used to describe how a set of application taxonomy mappings will change in over time or under differing circumstances. each set of changes is contained within a _Scenario_.  Therefore a Roadmap is a set of Scenarios used to describe changes to a set of applications.

Examples of roadmaps and their Scenarios might be:

- Operations Process vs. Product Roadmap
   - Current state (_scenario_)
   - 2025 Target (_scenario_)
   - 2020 Interim (_scenario_)
   - 2025 Alternative Plan (_scenario_)

In this example we can see a single roadmap with 4 associated Scenarios. _Note_ that Scenarios can be contradictory to allow for alternative views as to what future state may look like.

Each scenario is a grid, the axes of which are a hand-picked collection of measurables. For instance the columns maybe a selection of _Business Processes_ and the rows a selection of _Financial Products_ . This gives us a grid, each cell representing the intersection between a process and a function. Applications can then be associated to cells and given a rating, e.g. App: _Settler_ could be dropped into the cell _Settlement processing_/_Bonds_ with a rating of _Disinvest_.


## Roadmaps

To represent a roadmap Waltz requires:

- a name 
- a description
- an x-axis domain
- a y-axis domain
- a rating scheme

The domains currently limited to measurable categories and are fixed so that meaningful comparisons can be made between scenarios. If the axes domains could differ then comparisons would become largely meaningless. 

The rating scheme is required and used to provide the valid set of values used to rate applications in the scenario cells. 

## Scenarios

To represent Scenarios, Waltz needs:

- a name
- description
- type ( `CURRENT | INTERIM | TARGET` )
- effective date
- release status (`DRAFT | ACTIVE | DEPRECATED | OBSOLETE` )
- position ( to override the natural alphabetical order info )
- set of measurables defining x-axis
- set of measurables defining y-axis


## Implementation Details

See the [Schema Diagram](./roadmaps_schema_diagram.pdf) for more information 
([source](https://dbdiagram.io/d/5c8ffacaf7c5bb70c72f54ce))
