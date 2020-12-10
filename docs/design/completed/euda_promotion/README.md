# Design for EUDA promotion in Waltz

## Status:  IMPLEMENTED

## Motivation
EUDAs that exist in Waltz need to be promoted to full applications for use in logical flows, roadmaps, diagrams etc.

## Proposal

Promoted EUDAs will now be tracked using a new column on the 'end_user_application' table. 
An admin screen with waltz-grid and search for all EUDAs will allow user to select the euda for promotion, with a check to confirm the action.
Promoting a EUDA will create an application record and mark the EUDA as promoted. 
Change log entry will be created to note time euda was promoted.
It will also migrate involvements from the EUDA to the application.

Other changes:
- Repopulate already promoted eudas back into 'end_user_application' table with 'promoted' flag.
- Table in 'apps-section' filtered to exclude 'promoted' eudas.

