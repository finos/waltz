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

package org.finos.waltz.data.cost;

import org.finos.waltz.data.GenericSelector;
import com.khartec.waltz.model.cost.EntityCostKind;
import com.khartec.waltz.model.cost.ImmutableEntityCostKind;
import com.khartec.waltz.schema.tables.records.CostKindRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.schema.Tables.COST;
import static com.khartec.waltz.schema.Tables.COST_KIND;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class CostKindDao {

    private final DSLContext dsl;

    private RecordMapper<Record, EntityCostKind> TO_COST_KIND_MAPPER = r -> {
        CostKindRecord record = r.into(COST_KIND);
        return ImmutableEntityCostKind.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .isDefault(record.getIsDefault())
                .build();
    };


    @Autowired
    public CostKindDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<EntityCostKind> findAll(){
        return dsl
                .select(COST_KIND.fields())
                .from(COST_KIND)
                .fetchSet(TO_COST_KIND_MAPPER);
    }


    public Set<Tuple2<EntityCostKind, Integer>> findCostKindsBySelector(GenericSelector genericSelector){
        return dsl
                .select(COST_KIND.fields())
                .select(DSL.max(COST.YEAR))
                .from(COST_KIND)
                .innerJoin(COST).on(COST_KIND.ID.eq(COST.COST_KIND_ID))
                .where(COST.ENTITY_ID.in(genericSelector.selector())
                        .and(COST.ENTITY_KIND.eq(genericSelector.kind().name())))
                .groupBy(COST_KIND.fields())
                .fetchSet(r -> tuple(TO_COST_KIND_MAPPER.map(r), r.get(DSL.max(COST.YEAR))));
    }


    public EntityCostKind getById(Long costKindId) {
        return dsl
                .select(COST_KIND.fields())
                .from(COST_KIND)
                .where(COST_KIND.ID.eq(costKindId))
                .fetchOne(TO_COST_KIND_MAPPER);
    }
}
