---
layout: post
title:  "Tutorial: Creating Custom Environments"
date:   2021-04-28
categories: waltz
---

# Tutorial: Creating Conditional Surveys
In this tutorial we will look at the new _Custom Environments_ feature introduced as part of [Waltz 1.32](https://github.com/finos/waltz/releases/tag/1.32).


## Custom Environments
Custom environments can be used to group together databases and servers to express a user-defined environment - for example SIT, UAT etc.

## Demonstration

[![Waltz: Custom Environments](http://img.youtube.com/vi/fQY68pNTOJ4/0.jpg)](https://www.youtube.com/watch?v=fQY68pNTOJ4&list=PLGNSioXgrIEfJFJCTFGxKzfoDmxwPEap4)

## Usage
You must have an involvement to an application if you wish to create or edit its environments and associated assets, these can be
maintained in the `People Section`.

New environments can be registered by visiting the `Technology Section` of an application and selecting 'register a new custom environment'.
You must provide a name; the group field can be used to further group environments, but will take 'Default' if none is specified.
These environments are not shared between applications but must have a name that is unique from all other environments registered for
that application.


### Adding assets
Once an environment has been created you can select it to see the linked assets; environments can be compared by expanding
multiple rows at the same time. To add and remove servers and databases select the edit button.

The list of servers and databases in the search results is pre-populated based upon the parent application. You can search
for assets belonging to other applications by clicking 'change the focus application' at the bottom of the section. The search bar within the
tab can be used to filter results by name, environment etc.

Once added, the asset will appear in the tree view at the top of the page. To remove an asset click the trash icon.

If you would like to add more assets that belong to an application already in the tree, click the edit icon next to the application
name and the lists of servers and databases below will be updated.


### Removing an environment
Clicking 'cancel' while in the edit mode will bring you back to the tree view. This can be toggled to a table view, or by selecting
an item in the tree more information about the asset will be shown in a panel on the right.

To delete an environment select the 'remove' button, a confirmation message will appear. Deleting an environment will
also remove its associations to servers and databases. If you are sure you wish to proceed, click 'Remove', otherwise 'cancel' will return you
to the previous view.


### Export
An export of environment information can be found at the bottom of the section. An .xlsx report is produced with separate
tabs for servers and databases. If you wished to group these together the environment name can be used as this is unique for your
application.


## Conclusion
Custom environments should allow better management of assets linked to your application and help describe user-defined environments
which have a specific purpose.


If you have any suggestions for future enhancements please contact us via our [Github Issues](https://github.com/finos/waltz/issues) pages.


----

You may also be interested in checking out the complete [Waltz playlist](https://www.youtube.com/playlist?list=PLGNSioXgrIEfJFJCTFGxKzfoDmxwPEap4) on YouTube.