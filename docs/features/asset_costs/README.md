# Asset Costs


## Overview


## Model



### Schema

|Column|Description|Example|
|---|---|---|
| asset_code | link to `application.asset_code` | `1213-23` |
| year | year the cost relates to | `2017` |
| kind | type of the cost (see below) | `INFRASTRUCTURE` |
| amount | (decimal 10,2) amount |  `100.99` |
| currency | no longer used (see below) | `USD` |
| provenance | where this data came from  | `aptio` |


#### Column: `kind`

The kind of a cost can be configured at a system level via the `enum_value` table.  See the 
section entitled 'configuration'.


#### Column: `currency`

Waltz has recently (1.6) implemented a change which introduced a system level 
currency.  This has removed the need fo the column in this table and is scheduled
for removal in version 1.7  ( [Github issue](https://github.com/khartec/waltz/issues/2529))

The system property can be defined in the settings table, similar to:

```
INSERT INTO settings (name, value, restricted) 
  VALUES ('settings.asset-cost.default-currency', 'USD', false);
```

If not set it default to `EUR`.

### Configuration

Asset Cost kinds are are stored in the enum_value table against the `CostKind` type.

e.g.

| Type | Key | DisplayName | Description |
| --- | --- | --- | --- |
| CostKind | APPLICATION_DEVELOPMENT | Application Development | ... |
| CostKind | INFRASTRUCTURE | Infrastructure | ... |
| CostKind | PEOPLE | People Costs | ... |
| CostKind | CUMULATIVE | Cumulative | ... |
| CostKind | OTHER | Other | ... |


### Expected Future Changes

- migrate `asset_cost.asset_code` to a standard entity reference (kind & id)
- drop `asset_cost.currency` and rely on system default currency instead
- change `asset_cost.kind` to be defined by system admins (via `entity_enum`)



---
[Back to ToC](../README.md)
