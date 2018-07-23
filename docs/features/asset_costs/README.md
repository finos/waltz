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
| provenance | where this data came from  | `aptio` |



### Configuration

#### Kind

The kind of a cost can be configured at a system level via the `enum_value` table. .

e.g.

| Type | Key | DisplayName | Description |
| --- | --- | --- | --- |
| CostKind | APPLICATION_DEVELOPMENT | Application Development | ... |
| CostKind | INFRASTRUCTURE | Infrastructure | ... |
| CostKind | PEOPLE | People Costs | ... |
| CostKind | CUMULATIVE | Cumulative | ... |
| CostKind | OTHER | Other | ... |

#### Currency

Waltz stores currency as a system wide property.  Waltz does **not** intend to support 
multi-currency in the near future.

The system property can be defined in the `settings` table, similar to:

```
INSERT INTO settings (name, value, restricted) 
  VALUES ('settings.asset-cost.default-currency', 'USD', false);
```

If not set it default to `EUR`.



---
[Back to ToC](../README.md)
