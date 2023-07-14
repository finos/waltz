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

package org.finos.waltz.data.data_flow_decorator;

import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.data_flow_decorator.DataTypeDirectionKey;
import org.finos.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import org.finos.waltz.model.data_flow_decorator.ImmutableDecoratorRatingSummary;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.when;


@Repository
public class LogicalFlowDecoratorSummaryDao {


    private final DSLContext dsl;

    private static final LogicalFlow lf = LOGICAL_FLOW.as("lf");
    private static final org.finos.waltz.schema.tables.LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR.as("lfd");

    @Autowired
    public LogicalFlowDecoratorSummaryDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<DecoratorRatingSummary> summarizeInboundForSelector(Select<Record1<Long>> selector) {
        Condition condition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(selector)
                .and(LOGICAL_NOT_REMOVED);

        return summarizeForCondition(condition);
    }

    public List<DecoratorRatingSummary> summarizeOutboundForSelector(Select<Record1<Long>> selector) {
        Condition condition = LOGICAL_FLOW.SOURCE_ENTITY_ID.in(selector)
                .and(LOGICAL_NOT_REMOVED);

        return summarizeForCondition(condition);
    }


    public List<DecoratorRatingSummary> summarizeForAll() {
        return summarizeForCondition(LOGICAL_NOT_REMOVED);
    }


    private List<DecoratorRatingSummary> summarizeForCondition(Condition condition) {
        // this is intentionally TARGET only as we use to calculate auth source stats
        Condition dataFlowJoinCondition = LOGICAL_FLOW.ID
                .eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID);

        Collection<Field<?>> groupingFields = newArrayList(
                LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND,
                LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
                LOGICAL_FLOW_DECORATOR.RATING);

        Field<Integer> countField = DSL.count(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID).as("count");

        return dsl
                .select(groupingFields)
                .select(countField)
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(dsl.renderInlined(dataFlowJoinCondition))
                .where(dsl.renderInlined(condition))
                .groupBy(groupingFields)
                .fetch(r -> {
                    EntityKind decoratorEntityKind = EntityKind.valueOf(r.getValue(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND));
                    long decoratorEntityId = r.getValue(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID);

                    EntityReference decoratorRef = EntityReference.mkRef(decoratorEntityKind, decoratorEntityId);
                    AuthoritativenessRatingValue rating = AuthoritativenessRatingValue.of(r.getValue(LOGICAL_FLOW_DECORATOR.RATING));
                    Integer count = r.getValue(countField);

                    return ImmutableDecoratorRatingSummary.builder()
                            .decoratorEntityReference(decoratorRef)
                            .rating(rating)
                            .count(count)
                            .build();
                });
    }


    public int updateRatingsByCondition(AuthoritativenessRatingValue rating, Condition condition) {
        return dsl
                .update(LOGICAL_FLOW_DECORATOR)
                .set(LOGICAL_FLOW_DECORATOR.RATING, rating.value())
                .where(condition)
                .execute();
    }


    public Map<DataTypeDirectionKey, List<Long>> logicalFlowIdsByTypeAndDirection(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");

        Table<Record1<Long>> sourceApp = selector.asTable("source_app");
        Table<Record1<Long>> targetApp = selector.asTable("target_app");
        Field<Long> sourceAppId = sourceApp.field(0, Long.class);
        Field<Long> targetAppId = targetApp.field(0, Long.class);
        Field<String> flowTypeCase =
                when(sourceAppId.isNotNull()
                        .and(targetAppId.isNotNull()), inline("INTRA"))
                        .when(sourceAppId.isNotNull(), inline("OUTBOUND"))
                        .otherwise(inline("INBOUND"));
        Field<String> flowType = DSL.field("flow_type", String.class);

        Condition condition = sourceAppId
                .isNotNull()
                .or(targetAppId.isNotNull())
                .and(lf.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())
                        .and(lf.IS_REMOVED.isFalse()));

        return  dsl.select(
                    lfd.DECORATOR_ENTITY_ID,
                    flowTypeCase.as(flowType),
                    lf.ID)
                .from(lf)
                .innerJoin(lfd)
                .on(lf.ID.eq(lfd.LOGICAL_FLOW_ID)
                        .and(lfd.DECORATOR_ENTITY_KIND.eq(inline(EntityKind.DATA_TYPE.name()))))
                .leftJoin(sourceApp)
                .on(sourceAppId.eq(lf.SOURCE_ENTITY_ID))
                .leftJoin(targetApp)
                .on(targetAppId.eq(lf.TARGET_ENTITY_ID))
                .where(dsl.renderInlined(condition))
                .fetchGroups(
                        k -> DataTypeDirectionKey.mkKey(
                                k.get(lfd.DECORATOR_ENTITY_ID),
                                FlowDirection.valueOf(k.get(flowType))),
                        v -> v.get(lf.ID));

    }
}
