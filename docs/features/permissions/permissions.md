# Overview
Waltz has two levels of permissions. The first level are the coarse grained '_User Roles_', the second level is the data-driven, fine grained '_Involvement Permissions_'.

## User Roles
Coarse Grained roles not dependent on specific entities, instead they operate across all entities.

Roles are defined in the `ROLE` table and are classified as either _System_ roles or _Custom_ roles via the `is_custom` boolean column.

_System_ roles are pre-defined in Waltz and should not be removed. System roles control basic Waltz permissions such as `ADMIN`, `FLOW_EDITOR`, and `APP_EDITOR` and the Waltz code base will make direct reference to these system roles when checking permissions.

_Custom_ roles can be defined by administrators via the admin UI or directly in the database. Custom roles can be used in numerous places when configuring aspects of Waltz, for example:

- _Measurable Categories_, control who can edit measurable ratings within a category
- _Assessment Definitions_, who can edit assessment ratings for a given definition
- _Survey Approvers_, whilst issuing surveys the issuing user can provide a role which controls who can approve the issued survey instances
- _Legal Entity Relationships_, controls who can create relationships between legal entities and applications
- _Involvements_, although most involvements linking People to Entities come from external systems it is possible to assign them in the UI if the user has the appropriate role

**Note:** if a user is granted permission via a _user_role_ it will apply to all entities regardless of finer grained permissions.


## Role management
User Roles are granted at the user level via the `USER_ROLE` table. This table acts as a join table linking the user to the role. Changes to role assignments can be done via the UI or directly in the database.

The UI supports individual user updates or a simple bulk format. When updating user roles it is possible to add a supporting comment which will appear in the changelog associated to the target user, this changelog entry should detail why the permission is being assigned and indicate how approval was confirmed. The UI is restricted to only those users with either the `ADMIN` or `USER_ADMIN` system role assigned to them.

## Involvement Permissions
Basic role permissions are not sufficient to express more complex permission rules such as 'Restrict flow editing to the domain architects and asset owners of the applications being linked'. To support that we need to be able to express a triple containing:

- the _permission_ being sought (e.g. update logical data flows)
- the _involvement_/s required by the user/person to grant the permission (e.g. domain architect or asset owner)
- the _entity_ holding the involvements (e.g. applications)

Before looking at Involvement Permissions in more depth we will take a closer look at exactly how the Involvement mechanism is used by Waltz.

### Brief overview of Involvements
Involvements are used to assign people to entities via a named involvement kind. Waltz supports a wide range of entities that can be linked to people via the `INVOLVEMENT` table. These include the majority of high level entities in Waltz such as:

- Applications
- Change Initiatives
- Flows
- Measurables (and categories)
- Actors

The involvement table acts like a triple linking:

- a person (via employee_id)
- an entity (via entity_kind / entity_id), not a foreign key as
- an involvement kind (via kind_id)

The `INVOLVEMENT_KIND` table enumerates the types of involvement that can be captured, such as '_Asset Owner_' or '_Domain Architect_'.

Not all involvement kinds are applicable to all entity types. Each involvement kind should be restricted to a single kind (via subject_kind), e.g. 'Asset Owner' would be linked to Applications. The names need not be unique so if 'Asset Owner' is also required for other entity types (perhaps End User Applications) then another entry could be created - though the external_id field should differ.

Since involvements get used for permissions care should be taken about how the involvements are created. The involvement kind table has two fields to help control the usage of the involvement kind:

- `user_selectable` - a boolean flag to indicate if anyone can set this in the UI
  - this will typically be false as most involvements are imported from external systems
- `permitted_role` - as briefly discussed in the custom roles section above this would only allow users with the given role assignment to edit the involvements


Note: Involvements are not between users and entities as the person in question may not have ever logged on and would therefore not have a user account.

### Involvement Permissions Configuration
We will use our earlier example when discussing configuration: 'Restrict flow editing to the domain architects and asset owners of the applications being linked'

The involvement permissions are configured using _Permission Groups_ and _Involvement Groups_.

_Involvement Groups_ define and name a set of Involvement Kinds, for example we may define a group called '_Flow Owners_' which contains the _Architect_ and _Asset Owners_ involvements. By using an _Involvement Group_ we can reduce the amount of configuration required by simply referring to the group, rather than having to set up the more detailed configuration on a per _Involvement Kind_ basis.

_Permission Groups_ can be used to define subsets of entities involvement groups should be associated with. This is rarely used, as an organisation will typically have a consistent set of permissions across their estate, and the in-built default group identified by the `is_default` flag will be used. However, the _Permission Groups_ would allow you to set a different rules for subsets of entities (e.g. apps owned by the Investment Bank could only allow '_Domain Architects_' to define flows).

**Note**: precedence will be given to permission groups other than the default, however there is no guaranteed precedence if an entity exists in several groups.



These two groups are linked to specific permission rules in the `PERMISSION_GROUP_INVOLVEMENT` table. The table defines:

- the subject kind, the entity being permissioned
  - e.g. _Logical Data Flow_
- optionally, a qualifier entity to refine the subject
  - e.g. if the subject was _Assessment Ratings_ then the qualifier would be the _Assessment Definition_
- the parent kind, defines which entity holds the involvements
  - e.g. when controlling flow permissions the parent would be application as that has the involvements unlike the flows which tend not to have involements associated to them
- the operation, defines what kind of user operation the permission covers
  - e.g. `ADD`, `REMOVE`, `ATTEST`, `UPDATE`
- a reference to the involvement group
  - e.g. the '_Flow Owners_' group
- a reference to the permission group
  - e.g. a specific override group or, typically, a reference to the default group
