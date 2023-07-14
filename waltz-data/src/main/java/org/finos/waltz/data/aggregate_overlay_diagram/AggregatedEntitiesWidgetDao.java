package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AggregatedEntitiesWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAggregatedEntitiesWidgetDatum;
import org.finos.waltz.schema.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;
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
                                                             Select<Record1<Long>> inScopeEntityIdSelector,
                                                             Optional<LocalDate> targetStateDate) {

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Set<Long>> cellExtIdsToAggregatedEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                cellWithBackingEntities,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                targetStateDate);

        Map<Long, EntityReference> entityIdToRefMap = loadEntityIdToNameMap(aggregatedEntityKind, inScopeEntityIdSelector);

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


    private Map<Long, EntityReference> loadEntityIdToNameMap(EntityKind aggregatedEntityKind,
                                                             Select<Record1<Long>> inScopeEntityIdSelector) {
        switch (aggregatedEntityKind) {
            case APPLICATION:
                return loadIdToRefMap(
                        aggregatedEntityKind,
                        inScopeEntityIdSelector,
                        Tables.APPLICATION,
                        Tables.APPLICATION.ID,
                        Tables.APPLICATION.NAME,
                        Tables.APPLICATION.DESCRIPTION,
                        Tables.APPLICATION.ASSET_CODE);
            case CHANGE_INITIATIVE:
                return loadIdToRefMap(
                        aggregatedEntityKind,
                        inScopeEntityIdSelector,
                        Tables.CHANGE_INITIATIVE,
                        Tables.CHANGE_INITIATIVE.ID,
                        Tables.CHANGE_INITIATIVE.NAME,
                        Tables.CHANGE_INITIATIVE.DESCRIPTION,
                        Tables.CHANGE_INITIATIVE.EXTERNAL_ID);
            default:
                throw new IllegalArgumentException(format("Cannot fetch id to name map for entity kind: %s", aggregatedEntityKind));
        }
    }


    private Map<Long, EntityReference> loadIdToRefMap(EntityKind kind,
                                                      Select<Record1<Long>> inScopeEntityIdSelector,
                                                      Table<?> table,
                                                      TableField<? extends Record, Long> idField,
                                                      TableField<? extends Record, String> nameField,
                                                      TableField<? extends Record, String> descriptionField,
                                                      TableField<? extends Record, String> externalIdField) {
        return dsl
                .select(idField, nameField, descriptionField, externalIdField)
                .from(table)
                .where(idField.in(inScopeEntityIdSelector))
                .fetchMap(idField, r -> mkRef(
                        kind,
                        r.get(idField),
                        r.get(nameField),
                        r.get(descriptionField),
                        r.get(externalIdField)));
    }



}
