/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.data_flow_decorator;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableLogicalFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.NOT_REMOVED;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static java.util.stream.Collectors.toList;


@Repository
public class LogicalFlowDecoratorDao {

    private static final RecordMapper<Record, LogicalFlowDecorator> TO_DECORATOR_MAPPER = r -> {
        LogicalFlowDecoratorRecord record = r.into(LOGICAL_FLOW_DECORATOR);

        return ImmutableLogicalFlowDecorator.builder()
                .dataFlowId(record.getLogicalFlowId())
                .decoratorEntity(ImmutableEntityReference.builder()
                        .id(record.getDecoratorEntityId())
                        .kind(EntityKind.valueOf(record.getDecoratorEntityKind()))
                        .build())
                .rating(AuthoritativenessRating.valueOf(record.getRating()))
                .provenance(record.getProvenance())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };

    private static final Function<LogicalFlowDecorator, LogicalFlowDecoratorRecord> TO_RECORD = d -> {
        LogicalFlowDecoratorRecord r = new LogicalFlowDecoratorRecord();
        r.setDecoratorEntityKind(d.decoratorEntity().kind().name());
        r.setDecoratorEntityId(d.decoratorEntity().id());
        r.setLogicalFlowId(d.dataFlowId());
        r.setProvenance(d.provenance());
        r.setRating(d.rating().name());
        r.setLastUpdatedAt(Timestamp.valueOf(d.lastUpdatedAt()));
        r.setLastUpdatedBy(d.lastUpdatedBy());
        return r;
    };

    private final DSLContext dsl;


    @Autowired
    public LogicalFlowDecoratorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    // --- FINDERS ---

    public LogicalFlowDecorator getByFlowIdAndDecoratorRef(long flowId, EntityReference decoratorRef) {
        checkNotNull(decoratorRef, "decoratorRef cannot be null");

        return dsl
                .selectFrom(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(flowId))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorRef.kind().name()))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.eq(decoratorRef.id()))
                .fetchOne(TO_DECORATOR_MAPPER);
    }


    public List<LogicalFlowDecorator> findByEntityIdSelectorAndKind(EntityKind nodeKind,
                                                                    Select<Record1<Long>> nodeIdSelector,
                                                                    EntityKind decoratorEntityKind) {
        checkNotNull(nodeKind, "nodeKind cannot be null");
        checkNotNull(nodeIdSelector, "nodeIdSelector cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(nodeIdSelector)
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(nodeKind.name())))
                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.in(nodeIdSelector)
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(nodeKind.name())))
                .and(NOT_REMOVED)
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorEntityKind.name()))
                .fetch(TO_DECORATOR_MAPPER);
    }


    public List<LogicalFlowDecorator> findByDecoratorEntityIdSelectorAndKind(Select<Record1<Long>> decoratorEntityIdSelector,
                                                                             EntityKind decoratorKind) {
        checkNotNull(decoratorEntityIdSelector, "decoratorEntityIdSelector cannot be null");
        checkNotNull(decoratorKind, "decoratorKind cannot be null");

        Condition condition = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorKind.name())
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(decoratorEntityIdSelector));

        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(condition))
                .and(NOT_REMOVED)
                .fetch(TO_DECORATOR_MAPPER);
    }


    public List<LogicalFlowDecorator> findByFlowIds(Collection<Long> flowIds) {
        checkNotNull(flowIds, "flowIds cannot be null");

        Condition condition = LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.in(flowIds);

        return findByCondition(condition);
    }


    public Collection<LogicalFlowDecorator> findByFlowIdsAndKind(List<Long> flowIds, EntityKind decorationKind) {
        checkNotNull(flowIds, "flowIds cannot be null");
        checkNotNull(decorationKind, "decorationKind cannot be null");

        Condition condition = LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.in(flowIds)
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decorationKind.name()));

        return findByCondition(condition);
    }


    public Collection<LogicalFlowDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        Condition condition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .or(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector));

        return dsl.select(LOGICAL_FLOW_DECORATOR.fields())
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .and(NOT_REMOVED)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }


    // --- STATS ---

    public List<DecoratorRatingSummary> summarizeInboundForSelector(Select<Record1<Long>> selector) {
        Condition condition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(selector)
                .and(NOT_REMOVED);

        return summarizeForCondition(condition);
    }

    public List<DecoratorRatingSummary> summarizeOutboundForSelector(Select<Record1<Long>> selector) {
        Condition condition = LOGICAL_FLOW.SOURCE_ENTITY_ID.in(selector)
                .and(NOT_REMOVED);

        return summarizeForCondition(condition);
    }


    public List<DecoratorRatingSummary> summarizeForAll() {
        return summarizeForCondition(NOT_REMOVED);
    }



    // --- UPDATERS ---

    public int[] deleteDecorators(Long flowId, Collection<EntityReference> decoratorReferences) {
        List<LogicalFlowDecoratorRecord> records = decoratorReferences
                .stream()
                .map(ref -> {
                    LogicalFlowDecoratorRecord record = dsl.newRecord(LOGICAL_FLOW_DECORATOR);
                    record.setLogicalFlowId(flowId);
                    record.setDecoratorEntityId(ref.id());
                    record.setDecoratorEntityKind(ref.kind().name());
                    return record;
                })
                .collect(toList());
        return dsl
                .batchDelete(records)
                .execute();
    }


    @Deprecated
    // Replace with a method that removes decorators for a single flow
    public int removeAllDecoratorsForFlowIds(List<Long> flowIds) {
        return dsl.deleteFrom(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.in(flowIds))
                .execute();
    }


    public int[] addDecorators(Collection<LogicalFlowDecorator> decorators) {
        checkNotNull(decorators, "decorators cannot be null");

        List<LogicalFlowDecoratorRecord> records = decorators.stream()
                .map(TO_RECORD)
                .collect(toList());

        Query[] queries = records.stream().map(
                record -> DSL.using(dsl.configuration())
                        .insertInto(LOGICAL_FLOW_DECORATOR)
                        .set(record)
                        .onDuplicateKeyUpdate()
                        .set(record))
                .toArray(Query[]::new);
        return dsl.batch(queries).execute();
        // todo: in jOOQ 3.10.0 this can be written as follows #2979
        // return dsl.batchInsert(records).onDuplicateKeyIgnore().execute();
    }


    public int[] updateDecorators(Set<LogicalFlowDecorator> decorators) {
        Set<LogicalFlowDecoratorRecord> records = SetUtilities.map(decorators, TO_RECORD);
        return dsl.batchUpdate(records).execute();
    }


    // --- HELPERS ---

    private List<LogicalFlowDecorator> findByCondition(Condition condition) {
        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .from(LOGICAL_FLOW_DECORATOR)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
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
                    AuthoritativenessRating rating = AuthoritativenessRating.valueOf(r.getValue(LOGICAL_FLOW_DECORATOR.RATING));
                    Integer count = r.getValue(countField);

                    return ImmutableDecoratorRatingSummary.builder()
                            .decoratorEntityReference(decoratorRef)
                            .rating(rating)
                            .count(count)
                            .build();
                });
    }


    public int updateRatingsByCondition(AuthoritativenessRating rating, Condition condition) {
        return dsl
                .update(LOGICAL_FLOW_DECORATOR)
                .set(LOGICAL_FLOW_DECORATOR.RATING, rating.name())
                .where(condition)
                .execute();
    }
}
