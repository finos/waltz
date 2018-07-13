This document addresses how two features in Waltz may be used to facilitate a better understanding of the 
data within Waltz.

## Perspectives

Waltz, by design, typically stores simple facts against entities.  For example:

- _Application A_ supports _Function F_
- _Application B_ supports _Service Y_
- _Service Y_ relates to _Product P_

Sometimes a more nuanced view is required, to support this Waltz supports an extension to the above model 
known as **Perspectives**.  Currently perspectives allow you to refine an application view by combining two 
viewpoints (taxonomies) into a grid and allowing users to specify which combinations of viewpoint items 
are valid and which are not.  For example if we have a set of simple facts (for processing activity and 
recipient) stored against an application like so:

### Picture Here!


We can create a viewpoint relating TBC against TBC and specify the valid combinations.  
The TBC form the rows and the TBC the columns.

### Picture Here!

From this diagram we can see the third column (DB Direkt GmbH) participates in all activities except 
Investments. The colors indicates the alignment to the *rating scheme* (typically _Invest/Disinvest/Maintain_) 
which can be configured to suit the domain.  

Multiple perspectives can be defined in Waltz, but each can only related two viewpoints.


### Limitations of perspectives:

- Limited to two 'dimensions'
- Only available against applications (cannot record a perspective for other types of entities)
- No aggregate (summary) views yet. It's on backlog, but currently unprioritized
- Increases complexity and effort involved in mapping (n x n, rather than 2 x n)
- Uptake of existing functionality has been low due to above mentioned complexity/effort
- Perspectives are currently unaware of hierarchies and view the viewpoints as flat structures
  (as can be seen in the TBC columns).


## Drill Grid View

Waltz recently introduced a new way to view two viewpoints together at an aggregate level 
(i.e. Organisation Unit).  This allows users to pick two viewpoints (or select from pre-defined 
combinations) and navigate through the application data (simple facts) for those dimensions. 
 he drill grid has similarities to the familiar pivot table in Excel. 

In this example we will use TBC and TBC.  Initially the columnas are the top level 
TBCs and the rows represent TBC with the applications against those activities.
 
### Picture Here!
 
We can select a column or row-group to drill into more detail.  In this example we have drilled into 
the TBC (columns) and into the TBC sub-tree (the popup shows more detail about the selected cell)
 
### Picture Here!
 
### Limitations of drill grids:
- Currently only used against simple facts (not perspective aware yet, on backlog but nor prioritized)
- Row groups are dominant (they determine the apps to show, columns are there as supplemental detail)
- Can be quite complex to operate/interpret.
- Problems scaling to large numbers of applications
- Limited to operating on applications within an organisational unit.
