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

-- update parent id's based on external parent ids
UPDATE child
SET child.parent_id = parent.id
FROM measurable AS child
  INNER JOIN measurable AS parent ON parent.external_id = child.external_parent_id
WHERE child.measurable_category_id = 12;


--[SURVEYS]---

-- remove all
DELETE FROM survey_instance;
DELETE FROM survey_instance_recipient;
DELETE FROM survey_question_response;
DELETE FROM survey_run;
DELETE FROM survey_question;
DELETE FROM survey_template;

--[FLOWS]---
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
