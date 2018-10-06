# Static Panels

Static panels are a simple way to customise Waltz.  Within waltz various pages/widgets 
include a region for displaying configurable static information.  

Withing the database is a table" ```static_panel``` which has a structure
similar to:

- ```id```:  \<auto-assigned\>
- ```group```: see below
- ```title```: Text to appear on collapsible section banner
- ```icon```: Optional, font-awesome icon name (without the ```fa-``` prefix)
- ```priority```:  Number, lower numbers are drawn first
- ```width```:  0-12, corresponding with bootstrap ```col-md-[n]``` classes
- ```kind```:   either ```HTML``` and ```MARKDOWN```
- ```content```:  content to display


## Group

The group is used when selecting content to display on pages.
Currently assigned groups are:

- ```HOME```: Panels to appear on the home page
- ```HOME.CAPABILITY```: For the capability list page
- ```HOME.MEASURABLE.<measurable.category.id>```: For each measurable category
- ```HOME.ORG_UNIT```: For the organisational unit list page
- ```HOME.PERSON```: For the People overview page
- ```HOME.PROCESS```: For the Process list page
- ```HOME.PERFORMANCE```: For the Performance Metric overview page
- ```SECTION.ASSET_COSTS.ABOUT```: For an explanation of the asset costs section
- ```SECTION.COMPLEXITY.ABOUT```: For an explanation of the complexity calculation
- ```SECTION.AUTH_SOURCES.ABOUT```: For explanations about authoritative sources


_Developers Note_: This list can be obtained by running `grep -rh 'group-key' .` in the `waltz-ng/client` folder