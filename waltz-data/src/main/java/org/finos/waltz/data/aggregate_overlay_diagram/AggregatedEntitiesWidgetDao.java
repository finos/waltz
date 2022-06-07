package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AggregatedEntitiesWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAggregatedEntitiesWidgetDatum;
import org.finos.waltz.schema.Tables;
import org.jooq.*;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.selectCellMappingsForDiagram;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.lambda.tuple.Tuple.tuple;

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

        Map<String, Set<Long>> cellExtIdsToEntityIdsMap = loadCellExtIdToEntityMap(dsl, diagramId, aggregatedEntityKind, inScopeEntityIdSelector);

        Map<Long, String> entityIdToNameMap = loadEntityIdToNameMap(aggregatedEntityKind, inScopeEntityIdSelector);

        return cellExtIdsToEntityIdsMap
                .entrySet()
                .stream()
                .map(e -> {

                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();

                    Set<EntityReference> entityRefs = entityIds
                            .stream()
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


    protected static Map<String, Set<Long>> loadCellExtIdToEntityMap(DSLContext dsl,
                                                                     long diagramId,
                                                                     EntityKind aggregatedEntityKind,
                                                                     Select<Record1<Long>> inScopeEntityIdSelector) {

        Map<Long, List<Long>> measurableIdToEntityIds = findMeasurableIdToEntityIdMap(dsl, aggregatedEntityKind, inScopeEntityIdSelector);

        Set<Tuple3<String, String, Long>> record3s = selectCellMappingsForDiagram(dsl, diagramId)
                .fetchSet(r -> tuple(r.get(0, String.class), r.get(1, String.class), r.get(2, Long.class)));

        Map<String, Set<Tuple2<String, Long>>> relatedEntitiesByCellExtId = record3s
                .stream()
                .collect(groupingBy(t -> t.v1, mapping(t -> t.split1().v2, toSet())));

        return relatedEntitiesByCellExtId
                .entrySet()
                .stream()
                .map(e -> {
                    Set<Tuple2<String, Long>> value = e.getValue();
                    String cellExtId = e.getKey();

                    Map<String, Set<Long>> aggregatedEntitiesByKind = value
                            .stream()
                            .collect(groupingBy(t -> t.v1, mapping(t -> t.v2, toSet())));

                    Set<Long> measurableIds = aggregatedEntitiesByKind.getOrDefault(EntityKind.MEASURABLE.name(), emptySet());

                    Set<Long> entityIds = measurableIds
                            .stream()
                            .flatMap(mID -> measurableIdToEntityIds.getOrDefault(mID, emptyList()).stream())
                            .collect(toSet());

                    return tuple(cellExtId, entityIds);
                })
                .collect(toMap(k -> k.v1, k -> k.v2));
    }


    private static Map<Long, List<Long>> findMeasurableIdToEntityIdMap(DSLContext dsl,
                                                                       EntityKind aggregatedEntityKind,
                                                                       Select<Record1<Long>> inScopeEntityIdSelector) {
        switch (aggregatedEntityKind) {
            case APPLICATION:
                return loadMeasurableToAppIdsMap(dsl, inScopeEntityIdSelector);
            case CHANGE_INITIATIVE:
                return loadMeasurableToCIIdsMap(dsl, inScopeEntityIdSelector);
            default:
                throw new IllegalArgumentException(format("Cannot load measurable id to entity map for entity kind: %s", aggregatedEntityKind));
        }
    }


    private static Map<Long, List<Long>> loadMeasurableToCIIdsMap(DSLContext dsl,
                                                                  Select<Record1<Long>> inScopeEntityIdSelector) {

        SelectConditionStep<Record2<Long, Long>> aToB = dsl
                .selectDistinct(ENTITY_RELATIONSHIP.ID_B.as("measurable_id"), ENTITY_HIERARCHY.ID.as("ci_id"))
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(ENTITY_HIERARCHY).on(ENTITY_RELATIONSHIP.ID_A.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name())
                        .and(ENTITY_HIERARCHY.ID.in(inScopeEntityIdSelector)));

        SelectConditionStep<Record2<Long, Long>> bToA = dsl
                .selectDistinct(ENTITY_RELATIONSHIP.ID_A.as("measurable_id"), ENTITY_RELATIONSHIP.ID_B.as("ci_id"))
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(ENTITY_HIERARCHY).on(ENTITY_RELATIONSHIP.ID_A.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.MEASURABLE.name()))
                .and(ENTITY_HIERARCHY.ID.in(inScopeEntityIdSelector));

        return aToB.union(bToA)
                .fetchGroups(r -> r.get("measurable_id", Long.class), r -> r.get("ci_id", Long.class));
    }


    private static Map<Long, List<Long>> loadMeasurableToAppIdsMap(DSLContext dsl, Select<Record1<Long>> inScopeEntityIdSelector) {
        return dsl
                .selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID, MEASURABLE_RATING.ENTITY_ID)
                .from(ENTITY_HIERARCHY)
                .leftJoin(MEASURABLE_RATING).on(MEASURABLE_RATING.MEASURABLE_ID.eq(ENTITY_HIERARCHY.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name())))
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(MEASURABLE_RATING.ENTITY_ID.in(inScopeEntityIdSelector)))
                .fetchGroups(ENTITY_HIERARCHY.ANCESTOR_ID, MEASURABLE_RATING.ENTITY_ID);
    }

}
