package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AggregatedEntitiesWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAggregatedEntitiesWidgetDatum;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadEntityIdToRefMap;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;

@Repository
public class AggregatedEntitiesWidgetDao {

    private final DSLContext dsl;


    @Autowired
    public AggregatedEntitiesWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<AggregatedEntitiesWidgetDatum> findWidgetData(long diagramId,
                                                             EntityKind aggregatedEntityKind,
                                                             Select<Record1<Long>> inScopeEntityIdSelector,
                                                             Optional<LocalDate> targetStateDate) {

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Set<Long>> cellExtIdsToAggregatedEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                cellWithBackingEntities,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                targetStateDate);

        Map<Long, EntityReference> entityIdToRefMap = loadEntityIdToRefMap(dsl, aggregatedEntityKind, inScopeEntityIdSelector);

        return cellExtIdsToAggregatedEntities
                .entrySet()
                .stream()
                .map(e -> {

                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();

                    Set<EntityReference> entityRefs = entityIds
                            .stream()
                            .map(entityIdToRefMap::get)
                            .filter(Objects::nonNull)
                            .collect(toSet());

                    return ImmutableAggregatedEntitiesWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .aggregatedEntityReferences(entityRefs)
                            .build();
                })
                .filter(d -> !d.aggregatedEntityReferences().isEmpty())
                .collect(toSet());
    }

}
