package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableTargetCostWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.TargetCostWidgetDatum;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;
import static org.finos.waltz.schema.Tables.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class TargetAppCostWidgetDao {

    private static final Tuple2<BigDecimal, BigDecimal> ZERO_COST = tuple(BigDecimal.ZERO, BigDecimal.ZERO);
    private final DSLContext dsl;


    @Autowired
    public TargetAppCostWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<TargetCostWidgetDatum> findWidgetData(long diagramId,
                                                     Select<Record1<Long>> inScopeApplicationSelector,
                                                     LocalDate targetStateDate) {

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Set<Long>> cellExtIdsToAggregatedEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                cellWithBackingEntities,
                EntityKind.APPLICATION,
                inScopeApplicationSelector,
                Optional.empty());

        Set<Long> diagramAppIds = cellExtIdsToAggregatedEntities
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());

        Map<Long, Tuple2<BigDecimal, BigDecimal>> appToTargetStateCosts = fetchAppIdToTargetStateCostIndicator(
                targetStateDate,
                diagramAppIds);

        return cellExtIdsToAggregatedEntities
                .entrySet()
                .stream()
                .map(e -> {
                    String cellExtId = e.getKey();
                    Set<Long> appIds = e.getValue();

                    BigDecimal currentCost = appIds
                            .stream()
                            .map(v -> appToTargetStateCosts.getOrDefault(v, ZERO_COST).v1)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal targetCost = appIds
                            .stream()
                            .map(v -> appToTargetStateCosts.getOrDefault(v, ZERO_COST).v2)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ImmutableTargetCostWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .currentStateCost(currentCost)
                            .targetStateCost(targetCost)
                            .build();
                })
                .filter(d -> !(d.currentStateCost().equals(BigDecimal.ZERO) && d.targetStateCost().equals(BigDecimal.ZERO)))
                .collect(toSet());
    }


    private Map<Long, Tuple2<BigDecimal, BigDecimal>> fetchAppIdToTargetStateCostIndicator(LocalDate targetStateDate,
                                                                                           Set<Long> diagramApplicationIds) {

        Timestamp targetStateTimestamp = Timestamp.valueOf(targetStateDate.atStartOfDay());

        Field<BigDecimal> targetCost = DSL
                .when(DSL
                        .coalesce(
                                APPLICATION.ACTUAL_RETIREMENT_DATE,
                                APPLICATION.PLANNED_RETIREMENT_DATE)
                        .lt(targetStateTimestamp), DSL.val(BigDecimal.ZERO))
                .otherwise(COST.AMOUNT)
                .as("target_cost");

        Condition costCondition = APPLICATION.ID.in(diagramApplicationIds)
                .and(COST.YEAR.eq(2021));

        Condition costJoinCondition = COST.ENTITY_ID.eq(APPLICATION.ID)
                .and(COST.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(COST.COST_KIND_ID.eq(DSL
                        .select(COST_KIND.ID)
                        .from(COST_KIND)
                        .where(COST_KIND.IS_DEFAULT.isTrue())));

        SelectConditionStep<Record3<Long, BigDecimal, BigDecimal>> costStuff = dsl
                .selectDistinct(APPLICATION.ID, COST.AMOUNT, targetCost)
                .from(APPLICATION)
                .leftJoin(COST)
                .on(costJoinCondition)
                .where(dsl.renderInlined(costCondition));

        return costStuff.fetchMap(
                APPLICATION.ID,
                r -> tuple(r.get(COST.AMOUNT), r.get(targetCost)));
    }


}
