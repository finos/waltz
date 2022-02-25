---
layout: post
title:  "Developers: Database process enhancements"
date:   2022-02-18
categories: waltz dev jooq liquibase
---

# Intro

We've recently been focussed on making Waltz more welcoming to newcomers.  Part of that has been through enhancing our getting-started processes with the aid of some fantastic contributions from the Waltz community. In particular:

- Docker images are now published as part of each release via GitHub actions.  Thanks to [Ljubo Nikolic](https://github.com/ljubon) for this great [contribution](https://github.com/finos/waltz/pull/5801)
- We are working with [Canonical](https://canonical.com/) to offer Waltz as a [JuJu Charm](https://juju.is/).  See the [working group](https://github.com/finos/waltz/issues/5911) and this [sub-project](https://github.com/finos/waltz-integration-juju) for more information

However, in this post we wish to focus on how we are improving our documentation and taking a look at some database related process enhancements.

# Database documentation

**Liquibase**

Beginning in Waltz 1.40 we are adding table and columns level documentation to our database definitions.  To add this documentation we will be using two features of [Liquibase](https://liquibase.org/):

- [Table remarks](https://docs.liquibase.com/change-types/set-table-remarks.html) - sets table level documentation
- [Column remarks](https://docs.liquibase.com/change-types/set-column-remarks.html) - sets column level documentation

An example of what this looks like for a single table (e.g. `entity_hierarchy`) is shown below:

![liquibase](/blog/assets/images/database-remarks/liquibase.png)

Liquibase transforms these commands to the appropriate dbms specific statements.  These statements can be written to a DDL script or applied directly to the database.  After applying these changes we can see the documentation is now available:

![table](/blog/assets/images/database-remarks/table.png)

**jOOQ Codegen**

One unexpected benefit of documenting our database objects is that [jOOQ](https://www.jooq.org/) is taking the table and column docs and including them in the generated code.    

![jOOQ](/blog/assets/images/database-remarks/jooq.png)

As you can see this will be a great help in the dev process, especially when dealing wit less frequently used tables and columns.  


# Multi Vendor Database migration support

The above screenshots were taken whilst working on the issue:  [Entity Hierarchy: show level of both ancestor and descendant node #5916](https://github.com/finos/waltz/issues/5916).  This issue adds a new (non-nullable) column to our entity hierarchy table to represent the level of the descendant node.  We wished to ensure that Waltz correctly updates this column for any pre-existing data.  The update is performed by a Liquibase task which can run arbitrary sql.  However, the syntax of the update statements varies between database vendors so we need a few variants of the statement (the appropriate variant is selected by Liquibase via the `dbms` attribute on _changesets_).  

**jOOQ Translate**
 
Luckily the jOOQ website has a [superb page](https://www.jooq.org/translate/) which translates SQL statement written in one dialect to another.  Keen to give this a try we wrote the migration script for Postgres (1) and requested a translation to SQL Server (2).  We quickly tested the resultant statement to ensure it worked and included it in our liquibase script.   

![jOOQ Translate](/blog/assets/images/database-remarks/jooq-translate.png)

We will definitely be using this feature again as it saves so much effort.  As we are heavy users of jOOQ within Waltz we have huge confidence in the ability of this tool to generate correct output even for complex statements.

# Conclusion

We hope this has shown you how we are making steps to improve our development process by improving our documentation and streamlining our database migration script authoring.  If you have any questions reach out to us via our [GitHub issues page](https://github.com/finos/waltz/issues/new).  




