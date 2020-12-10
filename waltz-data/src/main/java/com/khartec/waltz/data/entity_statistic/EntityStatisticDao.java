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

package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.FindEntityReferencesByIdSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatistic;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticDao implements FindEntityReferencesByIdSelector {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition es = ENTITY_STATISTIC_DEFINITION.as("es");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");
    private static final com.khartec.waltz.schema.tables.Application app = APPLICATION.as("app");

    private final DSLContext dsl;


    private static final RecordMapper<? super Record, EntityStatistic> TO_COMPOUND_MAPPER = record
            -> ImmutableEntityStatistic.builder()
            .definition(EntityStatisticDefinitionDao.TO_DEFINITION_MAPPER.map(record))
            .value(EntityStatisticValueDao.TO_VALUE_MAPPER.map(record))
            .build();


    @Autowired
    public EntityStatisticDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        Condition condition = es.ACTIVE.eq(active)
                .and(es.ENTITY_VISIBILITY.eq(true))
                .and(esv.ENTITY_KIND.eq(ref.kind().name()))
                .and(esv.ENTITY_ID.eq(ref.id()))
                .and(esv.CURRENT.eq(true));

        return dsl.select(es.fields())
                .select(esv.fields())
                .from(es)
                .innerJoin(esv)
                .on(esv.STATISTIC_ID.eq(es.ID))
                .where(dsl.renderInlined(condition))
                .fetch(TO_COMPOUND_MAPPER);
    }


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl.select(es.ID, es.NAME, DSL.val(EntityKind.ENTITY_STATISTIC.name()))
                .from(es)
                .where(es.ID.in(selector))
                .fetch(TO_ENTITY_REFERENCE);
    }

}
