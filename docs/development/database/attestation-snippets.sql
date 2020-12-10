


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


-- CHANGES SINCE LAST ATTESTATION
select a.ek, a.ei, a.aek, cl.message, cl.operation, cl.created_at, cl.user_id
from change_log cl
inner join (select ai.parent_entity_kind ek,
                   ai.parent_entity_id ei,
                   ai.attested_entity_kind aek,
                   max(ai.attested_at) attested
            from attestation_instance ai
            where ai.parent_entity_id = 16869
              and ai.parent_entity_kind = 'APPLICATION'
              and ai.attested_at is not null
            group by ai.parent_entity_id,
                     ai.parent_entity_kind,
                     ai.attested_entity_kind) a
    on a.ek = cl.parent_kind and a.ei = cl.parent_id and a.aek = cl.child_kind
    where cl.created_at > a.attested
    and cl.operation in ('ADD', 'REMOVE', 'UPDATE');




-- MODIFIED SINCE LAST ATTESTATION ?
select ai.parent_entity_kind,
       ai.parent_entity_id,
       ai.attested_entity_kind,
       max(ai.attested_at) attested,
       max(cl.created_at) modified,
       case
          when (max(ai.attested_at) > max(cl.created_at)) then 'ATTESTED'
          else 'MODIFIED'
       end
from attestation_instance ai
left join change_log cl
         on cl.parent_kind = ai.parent_entity_kind and cl.parent_id = ai.parent_entity_id and cl.child_kind = ai.attested_entity_kind
where ai.parent_entity_id = 20962
and ai.parent_entity_kind = 'APPLICATION'
and ai.attested_at is not null
and cl.operation in ('ADD', 'REMOVE', 'UPDATE') -- not interested in unknown and attestations
group by ai.parent_entity_id,
         ai.parent_entity_kind,
         ai.attested_entity_kind;


