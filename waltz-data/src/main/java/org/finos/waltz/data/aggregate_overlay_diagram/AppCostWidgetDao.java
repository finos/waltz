package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.StreamUtilities.Siphon;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.CostWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableCostWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableMeasurableCostEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.MeasurableCostEntry;
import org.finos.waltz.schema.tables.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.CollectionUtilities.*;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class AppCostWidgetDao {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final AggregateOverlayDiagramCellData cd = AggregateOverlayDiagramCellData.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;
    private static final EntityHierarchy eh = EntityHierarchy.ENTITY_HIERARCHY;
    private static final MeasurableRating mr = MeasurableRating.MEASURABLE_RATING;
    private static final Measurable m = Measurable.MEASURABLE;
    private static final Allocation a = Allocation.ALLOCATION;
    private static final Cost c = Cost.COST;
    private static final CostKind ck = CostKind.COST_KIND;

    private final DSLContext dsl;

    @Autowired
    public AppCostWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    // cellExtId,
    public Set<CostWidgetDatum> findWidgetData(long diagramId,
                                               Set<Long> costKindIds,
                                               long allocationSchemeId,
                                               Select<Record1<Long>> inScopeApplicationSelector,
                                               Optional<LocalDate> targetStateDate) {

        Map<String, Set<Long>> cellExtIdsToBackingEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                diagramId,
                EntityKind.APPLICATION,
                inScopeApplicationSelector,
                targetStateDate);

        Set<MeasurableCostEntry> costData = fetchCostData(
                dsl,
                costKindIds,
                allocationSchemeId,
                inScopeApplicationSelector,
                cellExtIdsToBackingEntities);

        Map<Long, Collection<MeasurableCostEntry>> costDataByAppId = groupBy(
                costData,
                MeasurableCostEntry::appId);

        Map<Long, Collection<MeasurableCostEntry>> costDataByMeasurableId = groupBy(
                costData,
                MeasurableCostEntry::measurableId);

        return cellExtIdsToBackingEntities
                .entrySet()
                .stream()
                .map(kv -> processMeasurableBackingsForCell(
                        kv.getKey(),
                        kv.getValue(),
                        costDataByMeasurableId,
                        costDataByAppId))
                .filter(d -> !d.measurableCosts().isEmpty())
                .collect(toSet());
    }


    private CostWidgetDatum processMeasurableBackingsForCell(String cellRef,
                                                             Set<Long> backingMeasurableIds,
                                                             Map<Long, Collection<MeasurableCostEntry>> costDataByMeasurableId,
                                                             Map<Long, Collection<MeasurableCostEntry>> costDataByAppId) {

        // backing refs may not be measurables (i.e. app groups, org units, people)
        Set<MeasurableCostEntry> costMappings = backingMeasurableIds
                .stream()
                .flatMap(r -> processCostsForMeasurable(
                        costDataByMeasurableId.getOrDefault(r, emptySet()),
                        costDataByAppId)
                        .stream())
                .collect(toSet());

        return ImmutableCostWidgetDatum.builder()
                .cellExternalId(cellRef)
                .measurableCosts(costMappings)
                .build();
    }


    private Set<MeasurableCostEntry> processCostsForMeasurable(Collection<MeasurableCostEntry> costsForMeasurable,
                                                               Map<Long, Collection<MeasurableCostEntry>> costDataByAppId) {

        Siphon<MeasurableCostEntry> noCostsSiphon = mkSiphon(mce -> mce.overallCost() == null);
        Siphon<MeasurableCostEntry> noAllocationSiphon = mkSiphon(mce -> mce.allocationPercentage() == null);

        Set<MeasurableCostEntry> explicitCosts = costsForMeasurable
                .stream()
                .filter(noCostsSiphon)
                .filter(noAllocationSiphon)
                .map(d -> {
                    BigDecimal allocPercentage = BigDecimal.valueOf(d.allocationPercentage());

                    BigDecimal allocatedCost = allocPercentage
                            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                            .multiply(d.overallCost());

                    return ImmutableMeasurableCostEntry.copyOf(d).withAllocatedCost(allocatedCost);
                })
                .collect(toSet());

        Set<MeasurableCostEntry> implicitShareOfCost = noAllocationSiphon
                .stream()
                .map(d -> {
                    long appId = d.appId();
                    Collection<MeasurableCostEntry> otherRatings = costDataByAppId.getOrDefault(appId, emptySet());
                    if (otherRatings.size() == 1) {
                        // only one rating, therefore entire cost is on this measurable
                        return ImmutableMeasurableCostEntry.copyOf(d).withAllocatedCost(d.overallCost());
                    } else {
                        Collection<MeasurableCostEntry> othersWithAllocations = filter(otherRatings, r -> r.allocationPercentage() != null);
                        if (othersWithAllocations.isEmpty()) {
                            // no other allocations (but more than 1 rating)
                            BigDecimal otherRatingsCount = BigDecimal.valueOf(otherRatings.size());

                            BigDecimal remainingCost = d.overallCost()
                                    .divide(otherRatingsCount, 2, RoundingMode.HALF_UP);

                            return ImmutableMeasurableCostEntry.copyOf(d).withAllocatedCost(remainingCost);
                        } else {
                            Long overallAllocatedPercentage = sumInts(map(othersWithAllocations, MeasurableCostEntry::allocationPercentage));
                            BigDecimal unallocatedPercentage = BigDecimal.valueOf(100 - overallAllocatedPercentage);
                            BigDecimal numUnallocatedRatings = BigDecimal.valueOf(otherRatings.size() - othersWithAllocations.size());

                            BigDecimal shareOfRemainingCost = unallocatedPercentage
                                    .divide(numUnallocatedRatings, 2, RoundingMode.HALF_UP)
                                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                                    .multiply(d.overallCost());

                            //(entityRef, Costkind, Allocation, Cost, Implicit/Explicit)
                            return ImmutableMeasurableCostEntry.copyOf(d).withAllocatedCost(shareOfRemainingCost);
                        }
                    }
                })
                .collect(toSet());

        return union(explicitCosts, implicitShareOfCost);
    }


    private Set<MeasurableCostEntry> fetchCostData(DSLContext dsl,
                                                   Set<Long> costKindIds,
                                                   long allocationSchemeId,
                                                   Select<Record1<Long>> inScopeApplicationSelector,
                                                   Map<String, Set<Long>> expandedBackingForCells) {

        Set<Long> measurableIds = flattenToMeasurableIds(expandedBackingForCells);

        SelectConditionStep<Record5<Long, Long, Integer, Long, BigDecimal>> qry = dsl
                .select(mr.MEASURABLE_ID,
                        mr.ENTITY_ID,
                        a.ALLOCATION_PERCENTAGE,
                        ck.ID,
                        c.AMOUNT)
                .from(mr)
                .innerJoin(ck).on(ck.ID.in(costKindIds))
                .leftJoin(a).on(mr.ENTITY_ID.eq(a.ENTITY_ID)
                        .and(mr.ENTITY_KIND.eq(a.ENTITY_KIND))
                        .and(mr.MEASURABLE_ID.eq(a.MEASURABLE_ID)))
                .leftJoin(c).on(mr.ENTITY_ID.eq(c.ENTITY_ID)
                        .and(mr.ENTITY_KIND.eq(c.ENTITY_KIND))
                        .and(c.COST_KIND_ID.eq(ck.ID)))
                .where(mr.MEASURABLE_ID.in(measurableIds))
                .and(mr.ENTITY_ID.in(inScopeApplicationSelector))
                .and(a.ALLOCATION_SCHEME_ID.eq(allocationSchemeId));

        return qry
                .fetchSet(r -> ImmutableMeasurableCostEntry
                        .builder()
                        .measurableId(r.get(mr.MEASURABLE_ID))
                        .appId(r.get(mr.ENTITY_ID))
                        .allocationPercentage(r.get(a.ALLOCATION_PERCENTAGE))
                        .costKindId(r.get(ck.ID))
                        .overallCost(r.get(c.AMOUNT))
                        .build());
    }


    private Set<Long> flattenToMeasurableIds(Map<String, Set<Long>> expandedBackingForCells) {
        return expandedBackingForCells
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());
    }


    private Map<String, Set<EntityReference>> findExpandedBackingForCells(DSLContext dsl,
                                                                          long diagramId) {
        /*
        -- get expanded backing for cells
        select cell_external_id, cd.related_entity_kind, coalesce(eh.id, cd.related_entity_id)
        from aggregate_overlay_diagram_cell_data cd
        left join entity_hierarchy eh on eh.ancestor_id = cd.related_entity_id and eh.kind = cd.related_entity_kind
        where diagram_id = 1
         */
        Field<Long> refId = DSL.coalesce(eh.ID, cd.RELATED_ENTITY_ID).as("refId");

        SelectConditionStep<Record3<String, String, Long>> qry = dsl
                .select(cd.CELL_EXTERNAL_ID,
                        cd.RELATED_ENTITY_KIND,
                        refId)
                .from(cd)
                .leftJoin(eh).on(eh.KIND.eq(cd.RELATED_ENTITY_KIND).and(eh.ANCESTOR_ID.eq(cd.RELATED_ENTITY_ID)))
                .where(cd.DIAGRAM_ID.eq(diagramId));

        return qry
                .fetch(r -> tuple(
                    r.get(cd.CELL_EXTERNAL_ID),
                    readRef(r, cd.RELATED_ENTITY_KIND, refId)))
                .stream()
                .collect(groupingBy(
                        t -> t.v1,
                        mapping(
                            t -> t.v2,
                            toSet())));

    }


}
