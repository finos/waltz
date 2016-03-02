# Liquibase

Waltz uses [Liquibase](http://www.liquibase.org/index.html) to manage it's schema.
  
The changelog file follows the [best practice guidelines](http://www.liquibase.org/bestpractices.html) outlined
on the liquibase site.


## Change Ids

Id's have undergone several changes since project inception.  The current format is:

```<yyyymmdd>-<issueId>-<counter>```

For example:

```20160302-102-2``` can easily be read as the second change relating to issues 102 and was created on 2nd March 2016.
Strictly speaking the date is not required but it helps when searching for changes in a certain time range.


## Executing the changes:

Waltz provides sample files:
- ```migrate*.*```

Which you may copy and adapt to your environment.



