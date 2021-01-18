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

package com.khartec.waltz.data.cost;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.cost.EntityCost;
import com.khartec.waltz.model.cost.ImmutableEntityCost;
import com.khartec.waltz.schema.tables.records.CostRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.COST;


@Repository
public class CostDao {

    private final DSLContext dsl;

    private RecordMapper<Record, EntityCost> TO_COST_MAPPER = r -> {
        CostRecord record = r.into(COST);
        EntityReference ref = mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId());
        return ImmutableEntityCost.builder()
                .id(record.getId())
                .costKindId(record.getCostKindId())
                .entityReference(ref)
                .amount(record.getAmount())
                .year(record.getYear())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    @Autowired
    public CostDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<EntityCost> findByEntityReference(EntityReference ref){
        return dsl
                .selectFrom(COST)
                .where(COST.ENTITY_ID.eq(ref.id())
                        .and(COST.ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_COST_MAPPER);
    }


    public Set<EntityCost> findBySelectorForYear(GenericSelector genericSelector,
                                                 int year){
        return dsl
                .selectFrom(COST)
                .where(COST.ENTITY_ID.in(genericSelector.selector())
                        .and(COST.ENTITY_KIND.eq(genericSelector.kind().name())))
                .and(COST.YEAR.eq(year))
                .fetchSet(TO_COST_MAPPER);
    }


    public Set<EntityCost> findByCostKindIdAndSelectorForYear(long costKindId,
                                                             GenericSelector genericSelector,
                                                             int year){
        return dsl
                .selectFrom(COST)
                .where(COST.ENTITY_ID.in(genericSelector.selector())
                        .and(COST.ENTITY_KIND.eq(genericSelector.kind().name())))
                .and(COST.COST_KIND_ID.eq(costKindId))
                .and(COST.YEAR.eq(year))
                .fetchSet(TO_COST_MAPPER);
    }


    public Optional<Integer> findLatestYear() {
        return dsl
                .select(DSL.max(COST.YEAR))
                .from(COST)
                .fetchOptional(0, Integer.class);
    }

}
