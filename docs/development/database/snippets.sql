-- This file contains useful SQL snippets.
-- To add a snippet locate the correct --[SECTION]---
-- or add a new one if none of the existing sections
-- are appropriate.



--[MEASURABLES]---

-- show all measurables (with ratings) for apps
SELECT app.name, mr.rating, m.name, m.measurable_kind
FROM measurable_rating mr
  INNER JOIN application app ON app.id = mr.entity_id
  INNER JOIN measurable m ON mr.measurable_id = m.id
;

-- select ratings of a specific kind
SELECT mr.*
FROM measurable_rating mr
  INNER JOIN measurable m ON mr.measurable_id = m.id
WHERE
  m.measurable_kind = 'PROCESS'
;

-- update parent id's based on external parent ids (mssql)
UPDATE child
SET child.parent_id = parent.id
    FROM measurable AS child
         INNER JOIN measurable AS parent ON parent.external_id = child.external_parent_id
WHERE child.measurable_category_id = (select id from measurable_category where external_id = :categoryExtId)
  and parent.measurable_category_id =  (select id from measurable_category where external_id = :categoryExtId);



-- update parent id's based on external parent ids (postgres)
update measurable
set parent_id = d.pid
from (
    select c.id, p.id
    from measurable c
    inner join measurable p on p.external_id = c.external_parent_id and p.measurable_category_id = 18
    where c.measurable_category_id = 18) d (cid, pid)
where id = d.cid;


-- apps in and org unit without any ratings against a measurable category
select * from application
where id in (
    (
        select app.id from application app
        inner join entity_hierarchy eh on eh.id = app.organisational_unit_id
        where eh.ancestor_id = 3125
        and eh.kind = 'ORG_UNIT'
        and app.entity_lifecycle_status = 'ACTIVE'
    ) except (
        select mr.entity_id from measurable_rating mr
        inner join measurable m on m.id = mr.measurable_id
        inner join measurable_category mc on mc.id = m.measurable_category_id
        where mc.external_id = 'FUNCTION'
        and mr.entity_kind = 'APPLICATION'
    )
);


--[SURVEYS]---

-- remove all
DELETE FROM survey_instance;
DELETE FROM survey_instance_recipient;
DELETE FROM survey_question_response;
DELETE FROM survey_run;
DELETE FROM survey_question;
DELETE FROM survey_template;

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

--[FLOWS]---

-- produce graphviz like output for set of phys flows by assoc tag:
--   prefix with `digraph G { rankdir=LR;`
select distinct(concat('"', sa.name, '" -> "', ta.name, '"'))
from physical_flow pf
inner join logical_flow lf on pf.logical_flow_id = lf.id
inner join application sa on sa.id = lf.source_entity_id
inner join application ta on ta.id = lf.target_entity_id
inner join tag_usage tu on tu.entity_id = pf.id and tu.entity_kind = 'PHYSICAL_FLOW'
inner join tag t on t.id = tu.tag_id
where t.target_kind = 'PHYSICAL_FLOW';
--   postfix with `}`


-- find deleted logical flows which still have remaining physical flows
select distinct
	aSource.name,
	aSource.asset_code,
	ps.owning_entity_kind, 
	ps.owning_entity_id, 
	aTarget.name,
	aTarget.asset_code,
	pf.target_entity_kind, 
	pf.target_entity_id, 
	cl.message,
	cl.user_id,
	cl.created_at
from physical_specification ps
join physical_flow pf on pf.specification_id = ps.id
left join logical_flow lf on 
		lf.source_entity_kind = ps.owning_entity_kind
		and lf.source_entity_id = ps.owning_entity_id
		and lf.target_entity_kind = pf.target_entity_kind
		and lf.target_entity_id = pf.target_entity_id
join [application] aSource on aSource.id = ps.owning_entity_id
join [application] aTarget on aTarget.id = pf.target_entity_id
join change_log cl on 
	cl.parent_kind = 'APPLICATION' 
	and (cl.parent_id = ps.owning_entity_id)
	and cl.message like '%' + aSource.name + '%'
	and cl.message like '%' + aTarget.name + '%'
where 
	lf.id is null
	and ps.owning_entity_kind = 'APPLICATION'
	and pf.target_entity_kind = 'APPLICATION'
	and cl.message like 'Flow removed between:%'
	and cl.operation = 'REMOVE'
order by cl.created_at desc

-- Example recursive query (mssql) to show data flow lineage
WITH flow_cte (id, source_entity_kind, source_entity_id, target_entity_kind, target_entity_id, lvl)
AS (
  SELECT
    lf.id,
    lf.source_entity_kind,
    lf.source_entity_id,
    lf.target_entity_kind,
    lf.target_entity_id,
    0 AS lvl
  FROM logical_flow lf
  WHERE target_entity_id = 20506
  UNION ALL
  SELECT
    up.id,
    up.source_entity_kind,
    up.source_entity_id,
    up.target_entity_kind,
    up.target_entity_id,
    ds.lvl + 1
  FROM logical_flow up
    INNER JOIN flow_cte ds
      ON up.target_entity_kind = ds.source_entity_kind AND up.target_entity_id = ds.source_entity_id
  WHERE ds.lvl + 1 < 3
)
SELECT
    id
    source_entity_kind,
    source_entity_id,
    target_entity_kind,
    target_entity_id,
    lvl
FROM flow_cte;

-- All incoming/intra/outgoing phys flows to an OU
select
  src.name as 'source',
  trg.name as 'target',
  ps.name as 'name',
  ps.format,
  ps.external_id,
  pf.frequency,
  pf.transport,
  ps.description
from physical_specification ps
  inner join physical_flow pf on pf.specification_id = ps.id
  inner join logical_flow lf on lf.id = pf.logical_flow_id
  inner join application src on lf.source_entity_id = src.id
  inner join application trg on lf.target_entity_id = trg.id
where lf.is_removed = 0
      and lf.source_entity_kind = 'APPLICATION'
      and lf.target_entity_kind = 'APPLICATION'
      and ( src.organisational_unit_id in (select id from entity_hierarchy where ancestor_id = 4566 and kind = 'ORG_UNIT')
            OR -- 4566 is an OU id
            trg.organisational_unit_id in (select id from entity_hierarchy where ancestor_id = 4566 and kind = 'ORG_UNIT'));


-- Query to recover tags of flows that were replaced with new flows
insert into tag_usage
select distinct tu.tag_id, ei.entity_id, ei.entity_kind, max(tu.created_at) as 'created_at', tu.created_by, tu.provenance
from tag t
inner join tag_usage tu on tu.tag_id = t.id
inner join physical_flow pf on pf.id = tu.entity_id
inner join external_identifier ei on ei.external_id = pf.external_id and ei.entity_kind = 'PHYSICAL_FLOW'
where tu.entity_kind = 'PHYSICAL_FLOW'
and pf.entity_lifecycle_status = 'REMOVED'
and not exists (select * from tag_usage tu2 where tu2.tag_id = tu.tag_id and tu2.entity_id = ei.entity_id and tu2.entity_kind = ei.entity_kind)
group by tu.tag_id, ei.entity_id, ei.entity_kind, tu.created_by, tu.provenance