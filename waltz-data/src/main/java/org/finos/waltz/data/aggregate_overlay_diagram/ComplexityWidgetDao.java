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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;

@Repository
public class ComplexityWidgetDao {

    private static final AggregateOverlayDiagramCellData cd = AggregateOverlayDiagramCellData.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;
    private static final EntityHierarchy eh = EntityHierarchy.ENTITY_HIERARCHY;
    private static final MeasurableRating mr = MeasurableRating.MEASURABLE_RATING;
    private static final Measurable m = Measurable.MEASURABLE;
    private static final Allocation a = Allocation.ALLOCATION;
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
                                                     Long costKindId,
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
                costKindId,
                aggregatedEntityKind,
                cellExtIdsToAggregatedEntities);
    }


    private Set<ComplexityWidgetDatum> fetchComplexityData(DSLContext dsl,
                                                           Long costKindId,
                                                           EntityKind aggregatedEntityKind,
                                                           Map<String, Set<Long>> cellExtIdToAggEntities) {


        Set<Long> diagramEntityIds = cellExtIdToAggEntities.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());

        Map<Long, BigDecimal> entityIdToScoreMap = dsl
                .select(c.ENTITY_ID, c.SCORE)
                .from(c)
                .where(c.COMPLEXITY_KIND_ID.eq(costKindId))
                .and(c.ENTITY_ID.in(diagramEntityIds)
                        .and(c.ENTITY_KIND.eq(aggregatedEntityKind.name())))
                .fetchMap(r -> r.get(c.ENTITY_ID), r -> r.get(c.SCORE));

        return cellExtIdToAggEntities
                .entrySet()
                .stream()
                .map(e -> {

                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();

                    Set<ComplexityEntry> complexities = entityIds
                            .stream()
                            .map(id -> {

                                BigDecimal complexity = entityIdToScoreMap.get(id);

                                if (complexity == null) {
                                    return null;
                                } else {
                                    return ImmutableComplexityEntry
                                            .builder()
                                            .appId(id)
                                            .complexityScore(complexity)
                                            .build();
                                }
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
