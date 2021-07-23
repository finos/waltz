---
layout: post
title:  "Surveys: Bulk adding people to already issued surveys"
date:   2021-07-23
categories: waltz sql
---

# Background

We recently issued a batch of custom application surveys to several hundred participants.  Using the standard survey functionality we identified the applications (using an app group) and the participants by their involvement to the applications in the group.

The survey issuance parameters looked something like this:

* **Survey Template**: _Records Management Survey_
* **Apps In Scope**: App group '_A_'
* **Participant Involvment**: _Business data owner_ or _Application owner_

In Waltz we call this the 'survey run configuration'.

# Problem 

After the survey had been issued and participants had started filling in their responses it was decided that we wished to add additional people to all issued surveys.

We could have done this manually by editing the participant list for each survey, but that would have been time-consuming and error-prone.  Instead, we wrote some SQL to update the database.

# Update Statement

We used _Common Table Expressions_ (e.g. _with foo as (select ...)_) to break the query into several parts to keep it simple.

First we define the list of person ids for the additional survey instance **_editors_**.  This is simply a lookup into the person table based on user emails.

Next we get a list of survey **_instances_** which have been issued using the survey template.  In practice we had some additional constraints here to narrow the scope of the survey instances to update.

We can now calculate the **_required recipients_** as a simple cross join of the _editors_ and the _instances_.

Some of the surveys may already have had the new editors assigned, so we did not want to blindly add all _editors_ to the survey _instances_.  We needed to look at the currently **_existing_recipients_** and subtract those (using `except`) from the _required recipients_ giving us the **_missing_recipients_** for each instance.

Finally, we use the list of _missing recipients_ and instances to insert directly into the `survey_instance_recipient` table.

```[sql]
with editors as (
        select id
        from person
        where email in (
                     'person1@myorg.com',
                     'person1@myorg.com',
                     'person1@myorg.com')),
     instances as (
        select si.id
        from survey_instance si
        inner join survey_run sr on si.survey_run_id = sr.id
        inner join survey_template st on sr.survey_template_id = st.id
        where st.name = 'Records Management Survey'
     ),
     required_recipients as (
        select i.id survey_instance_id, e.id person_id
        from instances i
        cross join editors e -- cross join for cartesian product of editors and instances
     ),
     existing_recipients as (
         select survey_instance_id, person_id from survey_instance_recipient
         where survey_instance_id in (select id from instances)
     ),
     missing_recipients as (
         select *
         from required_recipients
         except (select * from existing_recipients)
     )
insert into survey_instance_recipient (survey_instance_id, person_id)
select * from missing_recipients;
```

# Conclusion

We hope this article has shown how Waltz can be updated relatively easily, even when there is no direct support for task.  It also shows how using CTE's can greatly increase the readability of more complex SQL statements.




----

You may also be interested in checking out the complete [Waltz playlist](https://www.youtube.com/playlist?list=PLGNSioXgrIEfJFJCTFGxKzfoDmxwPEap4) on YouTube.
