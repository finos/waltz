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
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.cost.EntityCost;
import org.finos.waltz.model.cost.ImmutableEntityCost;
import org.finos.waltz.schema.tables.records.CostRecord;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.JooqUtilities.selectorToCTE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.COST;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class CostDao {

    private final DSLContext dsl;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            COST.ENTITY_ID,
            COST.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    private static final RecordMapper<Record, EntityCost> TO_COST_MAPPER = r -> {
        CostRecord record = r.into(COST);
        EntityReference ref = mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId(), r.getValue(ENTITY_NAME_FIELD));
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
                .select(ENTITY_NAME_FIELD)
                .select(COST.fields())
                .from(COST)
                .where(COST.ENTITY_ID.eq(ref.id())
                        .and(COST.ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_COST_MAPPER);
    }


    public Set<EntityCost> findBySelector(GenericSelector genericSelector,
                                          int year){
        return dsl
                .select(ENTITY_NAME_FIELD)
                .select(COST.fields())
                .from(COST)
                .where(COST.ENTITY_ID.in(genericSelector.selector())
                        .and(COST.ENTITY_KIND.eq(genericSelector.kind().name())))
                .and(COST.YEAR.eq(year))
                .fetchSet(TO_COST_MAPPER);
    }


    public Set<EntityCost> findTopCostsForCostKindAndSelector(long costKindId,
                                                              int year,
                                                              GenericSelector genericSelector,
                                                              int limit){

        Condition cond = COST.ENTITY_ID.in(genericSelector.selector())
                .and(COST.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(COST.COST_KIND_ID.eq(costKindId))
                .and(COST.YEAR.eq(year));

        SelectSeekStep1<Record, BigDecimal> qry = dsl
                .select(ENTITY_NAME_FIELD)
                .select(COST.fields())
                .from(COST)
                .where(cond)
                .orderBy(COST.AMOUNT.desc());

        return qry
                .fetchStream()
                .map(TO_COST_MAPPER::map)
                .limit(limit)
                .collect(Collectors.toSet());
    }


    /**
     * @param costKindId  which cost to sum
     * @param year  which year to sum
     * @param selector  which entities to include in sum
     * @return  null if no costs, otherwise the total
     */
    public BigDecimal getTotalForKindAndYearBySelector(long costKindId,
                                                       Integer year,
                                                       GenericSelector selector) {
        Field<BigDecimal> total = DSL.sum(COST.AMOUNT).as("total");

        List<Long> appIds = dsl
                .fetch(selector.selector())
                .map(Record1::value1);

        Condition condition = COST.COST_KIND_ID.eq(costKindId)
                .and(COST.YEAR.eq(year))
                .and(COST.ENTITY_KIND.eq(selector.kind().name()))
                .and(COST.ENTITY_ID.in(appIds));

        SelectConditionStep<Record1<BigDecimal>> qry = dsl
                .select(total)
                .from(COST)
                .where(dsl.renderInlined(condition));

        return qry
                .fetchOne(total);
    }


    public Tuple2<Integer, Integer> getMappedAndMissingCountsForKindAndYearBySelector(Long costKindId,
                                                                                      Integer year,
                                                                                      GenericSelector genericSelector) {
        CommonTableExpression<Record1<Long>> appIds = selectorToCTE("app_ids", genericSelector);

        CommonTableExpression<Record1<Long>> appsWithCosts = DSL
                .name("apps_with_costs")
                .fields("id")
                .as(DSL
                    .select(COST.ENTITY_ID)
                    .from(COST)
                    .where(COST.COST_KIND_ID.eq(costKindId))
                    .and(COST.YEAR.eq(year))
                    .and(COST.ENTITY_KIND.eq(genericSelector.kind().name()))
                    .and(COST.ENTITY_ID.in(DSL.select(appIds.field(0, Long.class)).from(appIds))));

        Field<Integer> appCount = DSL.count().as("app_count");
        // the second count (apps with costs) relies on the sql count function omitting
        // nulls - in this case the failed left join to an actual cost
        Field<Integer> appsWithCostsCount = DSL.count(appsWithCosts.field(0)).as("apps_with_costs_count");

        SelectOnConditionStep<Record2<Integer, Integer>> qry = dsl
                .with(appIds)
                .with(appsWithCosts)
                .select(appCount,
                        appsWithCostsCount)
                .from(appIds)
                .leftJoin(dsl.renderInlined(appsWithCosts))
                .on(dsl.renderInlined(appIds.field(0, Long.class).eq(appsWithCosts.field(0, Long.class))));

        return qry
                .fetchOne(r -> tuple(
                        r.get(appsWithCostsCount),
                        r.get(appCount) - r.get(appsWithCostsCount)));
    }

}
