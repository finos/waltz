# Managed Documents

## Motivation

## Consequences

## Design

Managed documents consist of two main parts:
    
- Templates: define what data is being captured 
- Instances: store the responses 


### Templates

Managed Document Templates need to be:
 
 - [ ] versioned
 - [ ] support conditional sections
 - [ ] support multi-answers
 - [ ] owned
 - [x] have instance lifecycle configuration


#### Template 

Table: `managed_doc_template`

| Field | Type | Desc |
| --- | --- | --- |
| `id` | long | PK (seq) |
| `name` | string | name of template |


#### Template Version

Table: `managed_doc_template_version`

| Field | Type | Desc |
| --- | --- | --- |
| `id` | long | PK (seq) |
| `template_id` | long | FK >- `template:id` |
| `version` | string | version string, ideally in `x.y.z` format |
| `description` | string | name of template |
| `release_lifecycle_phase` | string (enum) | one of: `DRAFT, ACTIVE, DEPRECATED, OBSOLETE` |
| `template_form` | string | json representation of form |


#### Template ownership

Table: `managed_doc_template_owner`

| Field | Type | Desc |
| --- | --- | --- |
| `template_id` | long | PK FK >- `template:id` |
| `user_id` | string | PK, email |
| `role` | string enum | PK, one of: `ADMIN, EDITOR` |

Users marked as `EDITOR`'s can update template questions but perform lifecycle events



```
{
    template: "test",
    sections: [
        { 
            name: "about", 
            repeatable: false, 
            conditional: ""
        }
    ]
}
```


### Instances

Managed Document Instances need to:
 
 - be subject to workflow
 - be versioned
 - have defined editors/delegates etc
 