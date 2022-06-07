
package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagram;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AggregateOverlayDiagramUtilities {

    protected static final RecordMapper<? super Record, ? extends AggregateOverlayDiagram> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM);
        return ImmutableAggregateOverlayDiagram.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .svg(record.getSvg())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .aggregatedEntityKind(EntityKind.valueOf(record.getAggregatedEntityKind()))
                .build();
    };


    protected static Set<Tuple2<String, EntityReference>> loadExpandedCellMappingsForDiagram(DSLContext dsl,
                                                                                             long diagramId) {

        Field<Long> related_entity_id = DSL
                .coalesce(ENTITY_HIERARCHY.ID, AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)
                .as("related_entity_id");

        return dsl
                .selectDistinct(
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                        related_entity_id)
                .from(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA)
                .leftJoin(ENTITY_HIERARCHY)
                .on(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID.eq(ENTITY_HIERARCHY.ANCESTOR_ID))
                .and(ENTITY_HIERARCHY.KIND.eq(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND))
                .where(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.DIAGRAM_ID.eq(diagramId))
                .fetchSet(r -> tuple(
                        r.get(0, String.class), //cellExtId
                        readRef(r, r.field2(), r.field3())));
    }


    protected static SelectConditionStep<Record3<String, String, Long>> selectCellMappingsForDiagram2(DSLContext dsl,
                                                                                                      long diagramId) {

        return dsl
                .selectDistinct(
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)
                .from(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA)
                .where(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.DIAGRAM_ID.eq(diagramId));
    }


    protected static Map<String, Set<Long>> loadCellExtIdToAggregatedEntities(DSLContext dsl,
                                                                              long diagramId,
                                                                              EntityKind aggregatedEntityKind,
                                                                              Select<Record1<Long>> inScopeEntityIdSelector) {

        Set<Tuple2<String, EntityReference>> cellMappings = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Collection<EntityReference>> cellBackingEntitiesByCellExtId = groupBy(
                cellMappings,
                t -> t.v1,
                t -> t.v2);

        Set<Long> backingMeasurableEntityIds = cellMappings
                .stream()
                .map(Tuple2::v2)
                .filter(d -> d.kind() == EntityKind.MEASURABLE)
                .map(EntityReference::id)
                .collect(toSet());

        Map<Long, List<Long>> measurableIdToEntityIds = findMeasurableIdToAggregatedEntityIdMap(
                dsl,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                backingMeasurableEntityIds);

        return cellBackingEntitiesByCellExtId
                .entrySet()
                .stream()
                .map(e -> {
                    Set<EntityReference> value = fromCollection(e.getValue());
                    String cellExtId = e.getKey();

                    Map<EntityKind, Set<Long>> aggregatedEntitiesByKind = value
                            .stream()
                            .collect(groupingBy(
                                    EntityReference::kind,
                                    mapping(EntityReference::id, toSet())));

                    //only supports measurables at the moment
                    Set<Long> measurableIds = aggregatedEntitiesByKind.getOrDefault(EntityKind.MEASURABLE, emptySet());

                    Set<Long> entityIds = measurableIds
                            .stream()
                            .flatMap(mID -> measurableIdToEntityIds.getOrDefault(mID, emptyList()).stream())
                            .collect(toSet());

                    return tuple(cellExtId, entityIds);
                })
                .collect(toMap(k -> k.v1, k -> k.v2));
    }


    private static Map<Long, List<Long>> findMeasurableIdToAggregatedEntityIdMap(DSLContext dsl,
                                                                                 EntityKind aggregatedEntityKind,
                                                                                 Select<Record1<Long>> inScopeEntityIdSelector,
                                                                                 Set<Long> backingEntityIds) {

        switch (aggregatedEntityKind) {
            case APPLICATION:
                return loadMeasurableToAppIdsMap(dsl, inScopeEntityIdSelector, backingEntityIds);
            case CHANGE_INITIATIVE:
                return loadMeasurableToCIIdsMap(dsl, inScopeEntityIdSelector, backingEntityIds);
            default:
                throw new IllegalArgumentException(format("Cannot load measurable id to entity map for entity kind: %s", aggregatedEntityKind));
        }
    }


    private static Map<Long, List<Long>> loadMeasurableToCIIdsMap(DSLContext dsl,
                                                                  Select<Record1<Long>> inScopeEntityIdSelector,
                                                                  Set<Long> backingEntityIds) {

        SelectConditionStep<Record2<Long, Long>> aToB = dsl
                .selectDistinct(ENTITY_RELATIONSHIP.ID_B.as("measurable_id"), ENTITY_HIERARCHY.ID.as("ci_id"))
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(ENTITY_HIERARCHY).on(ENTITY_RELATIONSHIP.ID_A.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name())
                        .and(ENTITY_HIERARCHY.ID.in(inScopeEntityIdSelector)
                                .and(ENTITY_RELATIONSHIP.ID_B.in(backingEntityIds))));

        SelectConditionStep<Record2<Long, Long>> bToA = dsl
                .selectDistinct(ENTITY_RELATIONSHIP.ID_A.as("measurable_id"), ENTITY_RELATIONSHIP.ID_B.as("ci_id"))
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(ENTITY_HIERARCHY).on(ENTITY_RELATIONSHIP.ID_A.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.MEASURABLE.name())
                        .and(ENTITY_HIERARCHY.ID.in(inScopeEntityIdSelector)
                                .and(ENTITY_RELATIONSHIP.ID_B.in(backingEntityIds))));

        return aToB.union(bToA)
                .fetchGroups(
                        r -> r.get("measurable_id", Long.class),
                        r -> r.get("ci_id", Long.class));
    }


    private static Map<Long, List<Long>> loadMeasurableToAppIdsMap(DSLContext dsl,
                                                                   Select<Record1<Long>> inScopeEntityIdSelector,
                                                                   Set<Long> backingEntityReferences) {
        return dsl
                .selectDistinct(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.ENTITY_ID)
                .from(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(MEASURABLE_RATING.ENTITY_ID.in(inScopeEntityIdSelector))
                        .and(MEASURABLE_RATING.MEASURABLE_ID.in(backingEntityReferences)))
                .fetchGroups(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.ENTITY_ID);
    }
}
