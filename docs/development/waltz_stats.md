# Waltz Stats

Waltz growth stats can be tracked though the following queries:

### Users 

```
-- Unique users over a time period (e.g. last month):

select count(distinct user_id)
from access_log
where created_at > '01 Sep 2021'
and created_at < '01 Oct 2021';
```

### Hits 

```
-- Hits (pages/secions visited) over a time period (e.g. last month):

select count(*)
from access_log
where created_at > '01 Sep 2021'
and created_at < '01 Oct 2021';
  
  
-- Hits breakdown by page over a time period (e.g. last month):

select state, count(*)
from access_log
where created_at > '01 Sep 2021'
and created_at < '01 Oct 2021'
group by state
order by 1;
```
Note: Sections opened are tracked using the '|' in the `state` field. When counting views of 
a page, e.g. the application page, you do not want to include these in the total. The `routes.js` 
files can be used to identify which states belong to which entity/page.


### Edits 

```
-- Edits over a time period (e.g. last month):

select count(*)
from change_log
where created_at > '01 Sep 2021'
and created_at < '01 Oct 2021';
  ```

### Flows
```
-- Number of active flows with an unknown data type:

select count(*)
from logical_flow_decorator lfd
inner join logical_flow lf on lf.id = lfd.logical_flow_id
where decorator_entity_id in (select id from data_type where unknown = 1)
and decorator_entity_kind = 'DATA_TYPE'
and lf.entity_lifecycle_status != 'REMOVED'
and lf.is_removed = 0;
  
  
-- Number of active flows with a deprecated data type:

select count(*)
from logical_flow_decorator lfd
inner join logical_flow lf on lf.id = lfd.logical_flow_id
where decorator_entity_id in (
  select id
  from data_type
  where deprecated = 1)
and decorator_entity_kind = 'DATA_TYPE'
and lf.entity_lifecycle_status != 'REMOVED'
and lf.is_removed = 0;


-- Number of unknown data types removed from logical flows over a time period (e.g. last month)

select count(*)
from change_log cl
inner join (
    select '%Removed data types:%' +  CONVERT(varchar(10), id)  + '%' as pattern
    from data_type
    where unknown = 1) rm on cl.message like rm.pattern
where child_kind = 'DATA_TYPE'
and parent_kind = 'LOGICAL_DATA_FLOW'
and created_at > '01 Sep 2021'
and created_at < '01 Oct 2021';


-- Number of deprecated data types removed from logical flows over a time period (e.g. last month)

select count(*)
from change_log cl
inner join (
    select '%Removed data types:%' +  CONVERT(varchar(10), id)  + '%' as pattern
    from data_type
    where deprecated = 1) rm on cl.message like rm.pattern
where child_kind = 'DATA_TYPE'
and parent_kind = 'LOGICAL_DATA_FLOW'
and created_at > '01 Sep 2021'
and created_at < '01 Oct 2021'
```
Note: The count of `deprecated`/`unknown` data types removed is calculated using the data types at the 
time the query is run, this may be different to the list of `deprecated`/`unknown` data types at the time the 
change log was written. 


### Other

Other useful stats (to be calculated using previous queries):

```
% growth in users (e.g. last month) = (totalUsersToThisMonth - totalUsersToPreviousMonth) / totalUsersToPreviousMonth
```

```
% growth in hits (e.g. last month) = (totalHitsToThisMonth - totalHitsToPreviousMonth) / totalHitsToPreviousMonth
```

```
% growth in edits (e.g. last month) = (totalEditsToThisMonth - totalEditsToPreviousMonth) / totalEditsToPreviousMonth
```

```
Avg hits per user = totalHitsToDate / totalUsersToDate
```

