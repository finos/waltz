# Navigational Aids

## Overview

Hierarchies in Waltz (Organisational Units, People, Measurables, Data Types etc) can become large and difficult to
navigate by simply using a tree control.  Often organisations either already have, or can create, diagrams which 
can be re-purposed to become navigational aids.  

Navigational aids are SVG based diagrams with a special attributes declared on significant elements which allows
Waltz to hyperlink that element to a corresponding Waltz model element. 


### SVG Diagrams

Nav aids are currently declared in the `svg_diagram` table which looks like this:

|Column|Type|Sample|Description|
|---|---|---|---|
| id| long| 1 | auto assigned identifier |
| name| string| 'Org Chart' | Name of chart (displayed) |
| group | string | 'ORG_UNIT' | Type of diagram (see below) |
| priority | int | 1 | Determines tab order when multiple diagrams assigned to same group |
| description | string | 'About our org chart' | Description, shown below diagram |
| svg | string | '&lt;svg>....' | see below |
| key_property | string | 'unitid' | attribute to look for which contains an entity id |
| product | string | 'visio' | diagramming products encode custom data elements differently |

 
#### Column: `Group`

The group is used to determine where an svg diagram/s is rendered.  These groups are hard-coded into client side code and therefore all diagrams should align to one of the following:

|Group|Expected key value|Description|
|---|---|---|
|`ORG_TREE`|person, employee_id |Shown on the the person index page|
|`ORG_UNIT`|org unit, id|Shown on the organisational unit index page|
|`DATA_TYPE`|data type, code|Shown on the data type page|
|`NAVAID.MEASURABLE.<x>`|measurable, external id|Where `x` is the measurable category id, shown on each measurable tab on the viewpoint index page|

#### Columns: `svg` / `key_property` / `product`

The product should be either `visio` or `none`.  When set to `none` Waltz expects a property with the name `data-<key_property>` and a value corresponding to the the expected key value type listed in the above section.  For example:

```
...
<g data-unitid='4343'>
	<rect .../>
</g>
...
```

When the product name is set to `visio` Waltz will attempt to automatically translate Visio custom properties into these vendor neutral data properties.  To set a custom property in vision select the shape, bring up the context menu (right click) then select 'Define Shape Data', in the dialog set the label to be the 'expected key value' (see above) and set the value field appropriately.

**Note** Parsing Visio generated SVG can be quite slow, especially if the diagram is big.  For this reason it may be advantageous to initially set the `product` to `visio` and then capture the rendered output, replace the `svg` and change the `product` to `none`.   It is likely this process will be optimised in future releases.




