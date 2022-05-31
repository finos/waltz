package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.CountWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableCountWidgetDatum;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.*;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class AppCountWidgetDao {

    private static final Tuple2<Integer, Integer> ZERO_COUNT = tuple(0, 0);
    private final DSLContext dsl;


    @Autowired
    public AppCountWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<CountWidgetDatum> findWidgetData(long diagramId,
                                                Select<Record1<Long>> inScopeApplicationSelector,
                                                LocalDate targetStateDate) {

        Select<Record2<String, Long>> cellExtIdWithAppIdSelector = mkOverlayEntityCellAggregateEntitySelector(
                dsl,
                diagramId,
                EntityKind.APPLICATION);

        if (cellExtIdWithAppIdSelector == null) {
            // no cell mapping data so short circuit and give no results
            return Collections.emptySet();
        }

        Map<String, Set<Long>> cellExtIdsToAppIdsMap = fetchAndGroupEntityIdsByCellId(
                dsl,
                cellExtIdWithAppIdSelector);

        Set<Long> diagramApplicationIds = calcExactEntityIdsOnDiagram(
                dsl,
                cellExtIdsToAppIdsMap,
                inScopeApplicationSelector);

        Map<Long, Tuple2<Integer, Integer>> appToTargetStateCounts = fetchAppIdToTargetStatePresenceIndicator(
                targetStateDate,
                diagramApplicationIds);

        return cellExtIdsToAppIdsMap
                .entrySet()
                .stream()
                .map(e -> {
                    String cellExtId = e.getKey();
                    Set<Long> appIds = e.getValue();

                    int currentCount = appIds
                            .stream()
                            .mapToInt(v -> appToTargetStateCounts.getOrDefault(v, ZERO_COUNT).v1)
                            .sum();

                    int targetCount = appIds
                            .stream()
                            .mapToInt(v -> appToTargetStateCounts.getOrDefault(v, ZERO_COUNT).v2)
                            .sum();

                    return ImmutableCountWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .currentStateCount(currentCount)
                            .targetStateCount(targetCount)
                            .build();
                })
                .collect(toSet());
    }


    private Map<Long, Tuple2<Integer, Integer>> fetchAppIdToTargetStatePresenceIndicator(LocalDate targetStateDate,
                                                                                         Set<Long> diagramApplicationIdSelector) {
        Timestamp targetStateTimestamp = Timestamp.valueOf(targetStateDate.atStartOfDay());

        // actual: 2022, target: 2025  -> 0   (it has retired)
        // planned: 2026, target: 2025 -> 1   (not yet)
        // planned: -, target: 2025 -> 1      (also not yet)

        Field<Integer> isAppStillAlive = DSL
                .when(DSL
                        .coalesce(
                                APPLICATION.ACTUAL_RETIREMENT_DATE,
                                APPLICATION.PLANNED_RETIREMENT_DATE)
                        .lt(targetStateTimestamp), DSL.val(0))
                .otherwise(DSL.val(1))
                .as("has_retired");

        SelectConditionStep<Record2<Long, Integer>> countStuff = dsl
                .selectDistinct(APPLICATION.ID, isAppStillAlive)
                .from(APPLICATION)
                .where(dsl.renderInlined(APPLICATION.ID.in(diagramApplicationIdSelector)));

        return countStuff.fetchMap(
                APPLICATION.ID,
                r -> tuple(1, r.get(isAppStillAlive)));
    }


}
