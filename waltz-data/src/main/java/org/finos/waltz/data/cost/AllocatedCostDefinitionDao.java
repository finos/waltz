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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.DiffResult;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AllocationDerivation;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableMeasurableCostEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.MeasurableCostEntry;
import org.finos.waltz.model.cost.AllocatedCostDefinition;
import org.finos.waltz.model.cost.ImmutableAllocatedCostDefinition;
import org.finos.waltz.schema.tables.AllocationScheme;
import org.finos.waltz.schema.tables.Cost;
import org.finos.waltz.schema.tables.CostKind;
import org.finos.waltz.schema.tables.records.AllocatedCostDefinitionRecord;
import org.finos.waltz.schema.tables.records.CostRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.data.cost.CostUtils.calculateAllocatedCosts;
import static org.finos.waltz.model.DiffResult.mkDiff;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.ALLOCATED_COST_DEFINITION;
import static org.finos.waltz.schema.Tables.ALLOCATION;
import static org.finos.waltz.schema.Tables.ALLOCATION_SCHEME;
import static org.finos.waltz.schema.Tables.COST;
import static org.finos.waltz.schema.Tables.COST_KIND;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class AllocatedCostDefinitionDao {

    private static final Logger LOG = LoggerFactory.getLogger(AllocatedCostDefinitionDao.class);
    private final DSLContext dsl;
    private static final String PROVENANCE = "AllocatedCostPopulator";
    private static final Cost c = COST.as("c");
    private static final CostKind srcCostKind = COST_KIND.as("srcCostKind");
    private static final CostKind trgCostKind = COST_KIND.as("trgCostKind");
    private static final AllocationScheme allocScheme = ALLOCATION_SCHEME.as("allocScheme");
    private static final org.finos.waltz.schema.tables.AllocatedCostDefinition acd = ALLOCATED_COST_DEFINITION.as("acd");
    private static final org.finos.waltz.schema.tables.MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private static final org.finos.waltz.schema.tables.Measurable m = MEASURABLE.as("m");
    private static final org.finos.waltz.schema.tables.Allocation a = ALLOCATION.as("a");
    private static final RecordMapper<Record, AllocatedCostDefinition> TO_DOMAIN_MAPPER = r -> {

        AllocatedCostDefinitionRecord record = r.into(ALLOCATED_COST_DEFINITION);
        EntityReference allocScheme = mkRef(EntityKind.ALLOCATION_SCHEME, record.getAllocationSchemeId(), r.getValue(ALLOCATION_SCHEME.NAME));
        EntityReference sourceCost = mkRef(EntityKind.COST_KIND, record.getSourceCostKindId(), r.getValue(srcCostKind.NAME));
        EntityReference targetCost = mkRef(EntityKind.COST_KIND, record.getTargetCostKindId(), r.getValue(trgCostKind.NAME));

        return ImmutableAllocatedCostDefinition.builder()
                .id(record.getId())
                .allocationScheme(allocScheme)
                .sourceCostKind(sourceCost)
                .targetCostKind(targetCost)
                .build();
    };


    @Autowired
    public AllocatedCostDefinitionDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<AllocatedCostDefinition> findAll() {

        return dsl
                .select(acd.fields())
                .select(allocScheme.NAME)
                .select(srcCostKind.NAME)
                .select(trgCostKind.NAME)
                .from(acd)
                .innerJoin(allocScheme).on(acd.ALLOCATION_SCHEME_ID.eq(allocScheme.ID))
                .innerJoin(srcCostKind).on(acd.SOURCE_COST_KIND_ID.eq(srcCostKind.ID))
                .innerJoin(trgCostKind).on(acd.TARGET_COST_KIND_ID.eq(trgCostKind.ID))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public void allocateCostsByDefinition(AllocatedCostDefinition defn, Integer year) {

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        Set<MeasurableCostEntry> measurableCosts = findRequiredMeasurableCosts(defn, Optional.of(year));

        Map<Long, Collection<MeasurableCostEntry>> costDataByAppId = groupBy(
                measurableCosts,
                MeasurableCostEntry::appId);

        Set<MeasurableCostEntry> measurableCostsWithAllocatedCostValue = calculateAllocatedCosts(measurableCosts, costDataByAppId);

        dsl.transaction(ctx -> {

            DSLContext tx = ctx.dsl();

            Set<CostRecord> requiredRatingCosts = measurableCostsWithAllocatedCostValue
                    .stream()
                    .map(mc -> mkCostRecord(
                            tx,
                            defn.targetCostKind().id(),
                            EntityKind.MEASURABLE_RATING,
                            mc.measurableRatingId(),
                            mc.allocatedCost(),
                            mc.year(),
                            now))
                    .collect(toSet());

            Set<CostRecord> existingRatingCosts = tx
                    .selectFrom(c)
                    .where(c.COST_KIND_ID.eq(defn.targetCostKind().id()))
                    .fetchSet(r -> r.into(COST));

            DiffResult<CostRecord> diff = mkDiff(existingRatingCosts,
                    requiredRatingCosts,
                    k -> tuple(k.getEntityKind(), k.getEntityId(), k.getYear()),
                    (c1, c2) -> c1.getAmount().setScale(2, RoundingMode.HALF_UP).compareTo(c2.getAmount().setScale(2, RoundingMode.HALF_UP)) == 0);

            Collection<CostRecord> toUpdate = diff.differingIntersection();
            Collection<CostRecord> toAdd = diff.otherOnly();
            Collection<CostRecord> toRemove = diff.waltzOnly();

            int createdCosts = summarizeResults(tx.batchInsert(toAdd).execute());
            int removedCosts = summarizeResults(tx.batchDelete(toRemove).execute());

            int updatedCosts = summarizeResults(toUpdate
                    .stream()
                    .map(r -> tx
                            .update(c)
                            .set(c.AMOUNT, r.getAmount())
                            .set(c.LAST_UPDATED_AT, now)
                            .set(c.LAST_UPDATED_BY, "admin")
                            .set(c.PROVENANCE, PROVENANCE)
                            .where(c.COST_KIND_ID.eq(r.getCostKindId())
                                    .and(c.ENTITY_KIND.eq(r.getEntityKind())
                                            .and(c.ENTITY_ID.eq(r.getEntityId())
                                                    .and(c.YEAR.eq(r.getYear()))))))
                    .collect(collectingAndThen(toSet(), tx::batch))
                    .execute());

            LOG.debug(format("Created %d costs, Updated %d costs, Removed %d costs", createdCosts, updatedCosts, removedCosts));
        });
    }


    private Set<MeasurableCostEntry> findRequiredMeasurableCosts(AllocatedCostDefinition defn, Optional<Integer> year) {

        SelectConditionStep<Record1<Long>> categoryId = DSL
                .select(allocScheme.MEASURABLE_CATEGORY_ID)
                .from(allocScheme)
                .where(allocScheme.ID.eq(defn.allocationScheme().id()));

        Condition yearCondition = year
                .map(yr -> c.YEAR.eq(yr))
                .orElse(DSL.trueCondition());

        SelectConditionStep<Record7<Long, Long, Long, Integer, Long, BigDecimal, Integer>> qry = dsl
                .select(mr.ID,
                        mr.MEASURABLE_ID,
                        mr.ENTITY_ID,
                        a.ALLOCATION_PERCENTAGE,
                        c.COST_KIND_ID,
                        c.AMOUNT,
                        c.YEAR)
                .from(mr)
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID).and(m.MEASURABLE_CATEGORY_ID.eq(categoryId)))
                .innerJoin(c).on(mr.ENTITY_ID.eq(c.ENTITY_ID)
                        .and(mr.ENTITY_KIND.eq(c.ENTITY_KIND))
                        .and(c.COST_KIND_ID.eq(defn.sourceCostKind().id()))) // Only interested where the source app has a cost
                .leftJoin(a).on(mr.ID.eq(a.MEASURABLE_RATING_ID)
                        .and(a.ALLOCATION_SCHEME_ID.eq(defn.allocationScheme().id())))
                .where(yearCondition);

        return qry
                .fetchSet(r -> ImmutableMeasurableCostEntry
                        .builder()
                        .measurableId(r.get(mr.MEASURABLE_ID))
                        .appId(r.get(mr.ENTITY_ID))
                        .measurableRatingId(r.get(mr.ID))
                        .allocationPercentage(r.get(a.ALLOCATION_PERCENTAGE))
                        .allocationDerivation(r.get(a.ALLOCATION_PERCENTAGE) == null
                                ? AllocationDerivation.DERIVED
                                : AllocationDerivation.EXPLICIT)
                        .costKindId(r.get(c.COST_KIND_ID))
                        .year(r.get(c.YEAR))
                        .overallCost(r.get(c.AMOUNT))
                        .build());
    }


    private static CostRecord mkCostRecord(DSLContext tx,
                                           long costKindId,
                                           EntityKind entityKind,
                                           long entityId,
                                           BigDecimal allocatedCost,
                                           int year,
                                           Timestamp now) {

        CostRecord record = tx.newRecord(COST);
        record.setCostKindId(costKindId);
        record.setEntityKind(entityKind.name());
        record.setEntityId(entityId);
        record.setYear(year);
        record.setAmount(allocatedCost);
        record.setLastUpdatedAt(now);
        record.setLastUpdatedBy("admin");
        record.setProvenance(PROVENANCE);

        return record;
    }

}
