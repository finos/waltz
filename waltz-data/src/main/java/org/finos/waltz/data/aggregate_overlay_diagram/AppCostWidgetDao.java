package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.StreamUtilities.Siphon;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.schema.tables.*;
import org.immutables.value.Value;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class AppCostWidgetDao {

    @Value.Immutable
    interface MeasurableCostEntry {
        long appId();
        long measurableId();
        @Nullable
        Long costKindId();
        @Nullable
        Integer allocationPercentage();
        @Nullable
        BigDecimal overallCost();
    }

    private static final AggregateOverlayDiagramCellData cd = AggregateOverlayDiagramCellData.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;
    private static final EntityHierarchy eh = EntityHierarchy.ENTITY_HIERARCHY;
    private static final MeasurableRating mr = MeasurableRating.MEASURABLE_RATING;
    private static final Allocation a = Allocation.ALLOCATION;
    private static final Cost c = Cost.COST;
    private static final CostKind ck = CostKind.COST_KIND;

    private final DSLContext dsl;

    @Autowired
    public AppCostWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<?> foo(long diagramId,
                      Set<Long> costKindIds,
                      long allocationSchemeId,
                      Select<Record1<Long>> inScopeApplicationSelector) {

        Map<String, Set<EntityReference>> expandedBackingForCells = findExpandedBackingForCells(
                dsl,
                diagramId);

        Set<MeasurableCostEntry> costData = fetchCostData(
                dsl,
                costKindIds,
                allocationSchemeId,
                inScopeApplicationSelector,
                expandedBackingForCells);

        Map<Long, Collection<MeasurableCostEntry>> costDataByAppId = groupBy(
                costData,
                MeasurableCostEntry::appId);

        Map<Long, Collection<MeasurableCostEntry>> costDataByMeasurableId = groupBy(
                costData,
                MeasurableCostEntry::measurableId);

        expandedBackingForCells.entrySet()
                .stream()
                .map(kv -> {
                    processCell(kv.getKey(), kv.getValue(), costDataByMeasurableId, costDataByAppId);
                    return null;
                })
                .collect(toSet());

        System.out.println("Done");
        // load cell mappings  (cell -> [entity])
           // expand cell mappings if hierarchical

        // find all allocations for any mentioned measurable for apps on diagram
        // find all costs for apps on diagram

        // a -> 10%
        // a.b -> 30%
        // a.c -> 20%

        // [a] -> [a, a.b, a.c, a.d]
        // [a] -> ((0.1 * c1) + (0.3 * c1) + (0.2 * c1)) + ((..) + (..) ...)


        return null;
    }

    private void processCell(String cellRef,
                             Set<EntityReference> backingRefs,
                             Map<Long, Collection<MeasurableCostEntry>> costDataByMeasurableId,
                             Map<Long, Collection<MeasurableCostEntry>> costDataByAppId) {

        // backing refs may not be measurables (i.e. app groups, org units, people)

        backingRefs
                .stream()
                .filter(r -> r.kind() == EntityKind.MEASURABLE)
                .map(r -> processCostsForMeasurable(
                        r.id(),
                        costDataByMeasurableId.getOrDefault(r.id(), emptySet()),
                        costDataByAppId))
                .collect(toSet());


    }

    private Object processCostsForMeasurable(long measurableId,
                                             Collection<MeasurableCostEntry> costsForMeasurable,
                                             Map<Long, Collection<MeasurableCostEntry>> costDataByAppId) {

        System.out.printf(">M:%s  #:%d\n", measurableId, costsForMeasurable.size());
        Siphon<MeasurableCostEntry> noCostsSiphon = mkSiphon(mce -> mce.overallCost() == null);
        Siphon<MeasurableCostEntry> noAllocationSiphon = mkSiphon(mce -> mce.allocationPercentage() == null);
        BigDecimal total = costsForMeasurable
                .stream()
                .filter(noCostsSiphon)
                .filter(noAllocationSiphon)
                .map(d -> {
                    BigDecimal alloc = BigDecimal.valueOf(d.allocationPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    return d.overallCost().multiply(alloc);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.printf("<M:%s, T: %s\n", measurableId, total);
        return null;
    }

    private Set<MeasurableCostEntry> fetchCostData(DSLContext dsl,
                                                   Set<Long> costKindIds,
                                                   long allocationSchemeId,
                                                   Select<Record1<Long>> inScopeApplicationSelector,
                                                   Map<String, Set<EntityReference>> expandedBackingForCells) {
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


    private Set<Long> flattenToMeasurableIds(Map<String, Set<EntityReference>> expandedBackingForCells) {
        return expandedBackingForCells
                .values()
                .stream()
                .flatMap(xs -> xs
                        .stream()
                        .filter(x -> x.kind() == EntityKind.MEASURABLE)
                        .map(EntityReference::id))
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
