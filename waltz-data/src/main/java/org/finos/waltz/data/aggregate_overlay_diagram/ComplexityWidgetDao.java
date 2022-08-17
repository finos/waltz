package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ComplexityEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ComplexityWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableComplexityEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableComplexityWidgetDatum;
import org.finos.waltz.schema.tables.*;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class ComplexityWidgetDao {

    private static final Complexity c = Complexity.COMPLEXITY;
    private static final ComplexityKind ck = ComplexityKind.COMPLEXITY_KIND;

    private final DSLContext dsl;

    @Autowired
    public ComplexityWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    // cellExtId,
    public Set<ComplexityWidgetDatum> findWidgetData(long diagramId,
                                                     EntityKind aggregatedEntityKind,
                                                     Set<Long> costKindIds,
                                                     Select<Record1<Long>> inScopeEntityIdSelector) {

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Set<Long>> cellExtIdsToAggregatedEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                cellWithBackingEntities,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                Optional.empty());

        return fetchComplexityData(
                dsl,
                costKindIds,
                aggregatedEntityKind,
                cellExtIdsToAggregatedEntities);
    }


    private Set<ComplexityWidgetDatum> fetchComplexityData(DSLContext dsl,
                                                           Set<Long> costKindIds,
                                                           EntityKind aggregatedEntityKind,
                                                           Map<String, Set<Long>> cellExtIdToAggEntities) {


        Set<Long> diagramEntityIds = cellExtIdToAggEntities.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());

        Map<Long, List<Tuple2<Long, BigDecimal>>> entityIdToScoreMap = dsl
                .select(c.ENTITY_ID, c.COMPLEXITY_KIND_ID, c.SCORE)
                .from(c)
                .where(c.COMPLEXITY_KIND_ID.in(costKindIds))
                .and(c.ENTITY_ID.in(diagramEntityIds)
                        .and(c.ENTITY_KIND.eq(aggregatedEntityKind.name())))
                .fetchGroups(r -> r.get(c.ENTITY_ID), r -> tuple(r.get(c.COMPLEXITY_KIND_ID), r.get(c.SCORE)));

        return cellExtIdToAggEntities
                .entrySet()
                .stream()
                .map(e -> {

                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();

                    Set<ComplexityEntry> complexities = entityIds
                            .stream()
                            .flatMap(id -> {

                                List<Tuple2<Long, BigDecimal>> complexityScores = entityIdToScoreMap.getOrDefault(id, emptyList());

                                return complexityScores
                                        .stream()
                                        .map(t -> ImmutableComplexityEntry
                                                .builder()
                                                .appId(id)
                                                .complexityKindId(t.v1)
                                                .complexityScore(t.v2)
                                                .build());
                            })
                            .collect(toSet());

                    return ImmutableComplexityWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .complexities(complexities)
                            .build();
                })
                .collect(toSet());
    }

}
