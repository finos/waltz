# Static Panels

Static panels are a simple way to add content to key pages in Waltz.

Withing the database is a table" ```static_panel``` which has a structure
similar to:

- ```id```:  \<auto-assigned\>
- ```group```: see below
- ```title```: Text to appear on collapsible section banner
- ```icon```: Optional, font-awesome icon name (without the ```fa-``` prefix)
- ```priority```:  Number, lower numbers are drawn first
- ```width```:  0-12, corresponding with bootstrap ```col-md-[n]``` classes
- ```kind```:   only ```HTML``` currently supported.  Markdown support will be added in the future
- ```content```:  content to display


## Group

The group is used when selecting content to display on pages.
Currently assigned groups are:

- ```HOME```: Panels to appear on the home page
- ```HOME.CAPABILITY```: For the capability list page
- ```HOME.ORG_UNIT```: For the organisational unit list page
- ```HOME.PERSON```: For the People overview page
- ```HOME.PROCESS```: For the Process list page
- ```HOME.PERFORMANCE```: For the Performance Metric overview page




