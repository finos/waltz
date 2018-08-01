# Integrating Waltz and Neo4j

This article will explain how to export flow information 
into [Neo4J](http://www.neo4j.com) for analysis
 
 
## Setting up Neo4j

You may skip this section if you already have an instance of Neo4j.

Download Neo4j community server from
[Neo4j Download Center](https://neo4j.com/download-center/#releases).


Unzip the downloaded file and verify the installation via

    `%PATH_TO_NEO%/bin/neo4j console`

You should be able to browse (an empty db) via the [ console app](http://localhost:7474/browser/).
Once done, kill the console via the terminal.

## Exporting Waltz data from SQLServer

Neo4j can import data via csv files.  Multiple files can be provided for nodes, and 
relationships.  

In Waltz we will create two node files (for actors and applications) and
one relationship file (for logical flows)

### Node files

#### applications.csv 
```sql92
select
  concat('APPLICATION/', id) as ':ID',
  name,
  asset_code,
  lifecycle_phase,
  overall_rating,
  entity_lifecycle_status,
  'APPLICATION' as ':LABEL'
from application
where is_removed = 0;
```  

#### actors.csv
```sql92
select
  concat('ACTOR/', id) as ':ID',
  name,
  is_external as 'is_external:boolean',
  'ACTOR' as ':LABEL'
from actor;
```

### Relationship files

### flows.csv
```sql92
select
  concat(source_entity_kind, '/', source_entity_id) as ':START_ID',
  concat(target_entity_kind, '/', target_entity_id) as ':END_ID',
  'LOGICAL_FLOW' as ':TYPE'
from logical_flow
where entity_lifecycle_status <> 'REMOVED';
```

## Importing

Once the data is prepared we can use the `neo4j admin import` tool to bulk import
the nodes and relationships.

The import tool has many options, however, for ease, these options can be captured
into an external file:

#### options.txt
```
--id-type string
--nodes applications.csv
--nodes actors.csv
--relationships flows.csv
--database waltz-flows.db
```

#### Importing
Run the admin tool [import command](https://neo4j.com/docs/operations-manual/current/tools/import/) 
using a command like:
```
%PATH_TO_NEO%/bin/neo4j-admin import --f options.txt
```

Note, the target database (in this case `waltz-flows.db`) must be empty.

## Configuring Neo4j

To examine the contents of the newly created database (`waltz-flows.db`) we 
must configure neo4j to start the server using that db.  

In your `%PATH_TO_NEO%/conf/neo4j.conf` file set the following properties:

| property | value |
| --- | --- |
| `dbms.active_database` |  `waltz-flows.db` |
| `dbms.directories.data` | path to folder containing the database, by default this is the directory the import script was executed in |

You can now restart the console as before: 

    `%PATH_TO_NEO%/bin/neo4j console`
    
    
## Analysis

We now have our data loaded into Neo4j and start doing analysis.

### Shortest path between two nodes:

```cypher
MATCH (src:APPLICATION{asset_code:'12345-1'}), (targ:APPLICATION{asset_code:'98765-1'}),
      path = shortestpath((src) -[:LOGICAL_FLOW*]-> (targ))
RETURN path
ORDER BY LENGTH(path) DESC
LIMIT 1;
```


