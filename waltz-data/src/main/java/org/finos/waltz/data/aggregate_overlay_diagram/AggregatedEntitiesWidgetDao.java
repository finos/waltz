package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AggregatedEntitiesWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAggregatedEntitiesWidgetDatum;
import org.finos.waltz.schema.Tables;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.*;
import static org.finos.waltz.model.EntityReference.mkRef;

@Repository
public class AggregatedEntitiesWidgetDao {

    private final DSLContext dsl;


    @Autowired
    public AggregatedEntitiesWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<AggregatedEntitiesWidgetDatum> findWidgetData(long diagramId,
                                                             EntityKind aggregatedEntityKind,
                                                             Select<Record1<Long>> inScopeEntityIdSelector) {

        Select<Record2<String, Long>> cellExtIdWithEntityIdSelector = mkOverlayEntityCellAggregateEntitySelector(dsl, diagramId, aggregatedEntityKind);

        if (cellExtIdWithEntityIdSelector == null) {
            // no cell mapping data so short circuit and give no results
            return Collections.emptySet();
        }

        Map<String, Set<Long>> cellExtIdsToEntityIdsMap = fetchAndGroupEntityIdsByCellId(dsl, cellExtIdWithEntityIdSelector);

        Set<Long> diagramEntityIds = calcExactEntityIdsOnDiagram(
                dsl,
                cellExtIdsToEntityIdsMap,
                inScopeEntityIdSelector);

        Map<Long, String> entityIdToNameMap = loadEntityIdToNameMap(aggregatedEntityKind, inScopeEntityIdSelector);

        return cellExtIdsToEntityIdsMap
                .entrySet()
                .stream()
                .map(e -> {

                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();

                    Set<EntityReference> entityRefs = entityIds
                            .stream()
                            .filter(diagramEntityIds::contains)
                            .map(id -> mkRef(aggregatedEntityKind, id, entityIdToNameMap.get(id)))
                            .filter(ref -> ref.name().isPresent())
                            .collect(toSet());

                    return ImmutableAggregatedEntitiesWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .aggregatedEntityReferences(entityRefs)
                            .build();
                })
                .filter(d -> !d.aggregatedEntityReferences().isEmpty())
                .collect(toSet());
    }


    private Map<Long, String> loadEntityIdToNameMap(EntityKind aggregatedEntityKind, Select<Record1<Long>> inScopeEntityIdSelector) {
        switch (aggregatedEntityKind) {
            case APPLICATION:
                return loadIdToNameMap(inScopeEntityIdSelector, Tables.APPLICATION, Tables.APPLICATION.ID, Tables.APPLICATION.NAME);
            case CHANGE_INITIATIVE:
                return loadIdToNameMap(inScopeEntityIdSelector, Tables.CHANGE_INITIATIVE, Tables.CHANGE_INITIATIVE.ID, Tables.CHANGE_INITIATIVE.NAME);
            default:
                throw new IllegalArgumentException(format("Cannot fetch id to name map for entity kind: %s", aggregatedEntityKind));
        }
    }


    private Map<Long, String> loadIdToNameMap(Select<Record1<Long>> inScopeEntityIdSelector,
                                              Table<?> table,
                                              TableField<? extends Record, Long> idField,
                                              TableField<? extends Record, String> nameField) {
        return dsl
                .select(idField, nameField)
                .from(table)
                .where(idField.in(inScopeEntityIdSelector))
                .fetchMap(idField, nameField);
    }
}
