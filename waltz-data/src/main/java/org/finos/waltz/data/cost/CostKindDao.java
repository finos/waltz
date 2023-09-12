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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.cost.CostKindWithYears;
import org.finos.waltz.model.cost.ImmutableCostKindWithYears;
import org.finos.waltz.schema.tables.records.CostKindRecord;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.model.cost.EntityCostKind;
import org.finos.waltz.model.cost.ImmutableEntityCostKind;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.ListUtilities.filter;
import static org.finos.waltz.schema.Tables.COST;
import static org.finos.waltz.schema.Tables.COST_KIND;


@Repository
public class CostKindDao {

    private static final RecordMapper<Record, EntityCostKind> TO_COST_KIND_MAPPER = r -> {
        CostKindRecord record = r.into(COST_KIND);
        return ImmutableEntityCostKind.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .subjectKind(EntityKind.valueOf(record.getSubjectKind()))
                .isDefault(record.getIsDefault())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public CostKindDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<CostKindWithYears> findAll(){
        return findCostKindsByCondition(DSL.trueCondition());
    }


    public Set<CostKindWithYears> findCostKindsBySelector(GenericSelector genericSelector) {

        Condition cond = COST.ENTITY_ID.in(genericSelector.selector())
                .and(COST.ENTITY_KIND.eq(genericSelector.kind().name()));

        return findCostKindsByCondition(cond);
    }


    private Set<CostKindWithYears> findCostKindsByCondition(Condition cond){
        return dsl
                .selectDistinct(COST_KIND.fields())
                .select(COST.YEAR)
                .from(COST_KIND)
                .leftJoin(COST).on(COST_KIND.ID.eq(COST.COST_KIND_ID))
                .where(cond)
                .orderBy(COST.YEAR.desc())
                .fetchGroups(
                        TO_COST_KIND_MAPPER,
                        r -> r.get(COST.YEAR))
                .entrySet()
                .stream()
                .map(kv -> {
                    List<Integer> years = filter(Objects::nonNull, kv.getValue());
                    return ImmutableCostKindWithYears
                            .builder()
                            .costKind(kv.getKey())
                            .years(years)
                            .build();
                })
                .collect(toSet());
    }


    public EntityCostKind getById(Long costKindId) {
        return dsl
                .select(COST_KIND.fields())
                .from(COST_KIND)
                .where(COST_KIND.ID.eq(costKindId))
                .fetchOne(TO_COST_KIND_MAPPER);
    }
}
