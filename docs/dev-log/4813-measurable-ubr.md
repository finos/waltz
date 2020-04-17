# Measurables: add owning org unit 

[4813](https://github.com/finos/waltz/issues/4813)

# 20200416

- Think it's going to be difficult to deal with measurables that are supposed to inherit their ou alignments.
- 

## Useful snippets

```sql

-- basic implementation, only looks at OU tree
with
    ou_hier as (select id from entity_hierarchy where ancestor_id = 20 and kind = 'ORG_UNIT')
select id, name, organisational_unit_id
from measurable
where organisational_unit_id in (select id from ou_hier);


-- trying to deal with holes in m.ou mappings.  However this isn't working out:
select eh.level, eh.id, pm.name, ou.name
from entity_hierarchy eh
inner join measurable pm on pm.id = eh.ancestor_id
left join organisational_unit ou on pm.organisational_unit_id = ou.id
where eh.id = 7 and eh.kind = 'MEASURABLE'
and ou.id is not null
group by eh.id, eh.level, pm.name, ou.name
order by eh.level desc

```