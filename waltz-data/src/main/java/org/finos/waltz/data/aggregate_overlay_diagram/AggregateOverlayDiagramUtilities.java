
package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramKind;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.MeasurableRatingPlannedDecommission;
import org.finos.waltz.schema.tables.MeasurableRatingReplacement;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.DATA_TYPE_USAGE;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_REPLACEMENT;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AggregateOverlayDiagramUtilities {

    private static final MeasurableRating mr = MEASURABLE_RATING;
    private static final Application app = APPLICATION;
    private static final MeasurableRatingPlannedDecommission mrpd = MEASURABLE_RATING_PLANNED_DECOMMISSION;
    private static final MeasurableRatingReplacement mrp = MEASURABLE_RATING_REPLACEMENT;

    protected static final RecordMapper<? super Record, ? extends AggregateOverlayDiagram> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM);
        return ImmutableAggregateOverlayDiagram.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .layoutData(record.getLayoutData())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .aggregatedEntityKind(EntityKind.valueOf(record.getAggregatedEntityKind()))
                .diagramKind(OverlayDiagramKind.valueOf(record.getDiagramKind()))
                .status(ReleaseLifecycleStatus.valueOf(record.getStatus()))
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


    protected static Map<String, Set<Long>> loadCellExtIdToAggregatedEntities(DSLContext dsl,
                                                                              Set<Tuple2<String, EntityReference>> cellMappings,
                                                                              EntityKind aggregatedEntityKind,
                                                                              Select<Record1<Long>> inScopeEntityIdSelector,
                                                                              Optional<LocalDate> targetStateDate) {


        Set<Long> backingMeasurableEntityIds = toMeasurableIds(cellMappings);
        Set<Long> backingDataTypeEntityIds = toDataTypeIds(cellMappings);

        Map<Long, List<Long>> measurableIdToEntityIds = findMeasurableIdToAggregatedEntityIdMap(
                dsl,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                backingMeasurableEntityIds,
                targetStateDate);

        Map<Long, List<Long>> dataTypeIdToEntityIds = findDataTypeIdToAggregatedEntityIdMap(
                dsl,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                backingDataTypeEntityIds);

        Map<String, Collection<EntityReference>> cellBackingEntitiesByCellExtId = groupBy(
                cellMappings,
                t -> t.v1,
                t -> t.v2);

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
                    Set<Long> dataTypeIds = aggregatedEntitiesByKind.getOrDefault(EntityKind.DATA_TYPE, emptySet());

                    Set<Long> entityIdsViaMeasurable = measurableIds
                            .stream()
                            .flatMap(mID -> measurableIdToEntityIds.getOrDefault(mID, emptyList()).stream())
                            .collect(toSet());

                    Set<Long> entityIdsViaDataType= dataTypeIds
                            .stream()
                            .flatMap(dID -> dataTypeIdToEntityIds.getOrDefault(dID, emptyList()).stream())
                            .collect(toSet());

                    return tuple(cellExtId, union(entityIdsViaMeasurable, entityIdsViaDataType));
                })
                .collect(toMap(k -> k.v1, k -> k.v2));
    }


    public static Set<Long> toMeasurableIds(Set<Tuple2<String, EntityReference>> cellMappings) {
        return cellMappings
                .stream()
                .map(Tuple2::v2)
                .filter(d -> d.kind() == EntityKind.MEASURABLE)
                .map(EntityReference::id)
                .collect(toSet());
    }


    public static Set<Long> toDataTypeIds(Set<Tuple2<String, EntityReference>> cellMappings) {
        return cellMappings
                .stream()
                .map(Tuple2::v2)
                .filter(d -> d.kind() == EntityKind.DATA_TYPE)
                .map(EntityReference::id)
                .collect(toSet());
    }


    public static Map<Long, List<Long>> findMeasurableIdToAggregatedEntityIdMap(DSLContext dsl,
                                                                                 EntityKind aggregatedEntityKind,
                                                                                 Select<Record1<Long>> inScopeEntityIdSelector,
                                                                                 Set<Long> backingEntityIds,
                                                                                 Optional<LocalDate> targetStateDate) {

        switch (aggregatedEntityKind) {
            case APPLICATION:
                return loadMeasurableToAppIdsMap(dsl, inScopeEntityIdSelector, backingEntityIds, targetStateDate);
            case CHANGE_INITIATIVE:
                return loadMeasurableToCIIdsMap(dsl, inScopeEntityIdSelector, backingEntityIds);
            default:
                throw new IllegalArgumentException(format("Cannot load measurable id to entity map for entity kind: %s", aggregatedEntityKind));
        }
    }


    private static Map<Long, List<Long>> findDataTypeIdToAggregatedEntityIdMap(DSLContext dsl,
                                                                               EntityKind aggregatedEntityKind,
                                                                               Select<Record1<Long>> inScopeEntityIdSelector,
                                                                               Set<Long> backingEntityIds) {

        switch (aggregatedEntityKind) {
            case APPLICATION:
                return loadDataTypeToAppIdsMap(dsl, inScopeEntityIdSelector, backingEntityIds);
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


    public static Map<Long, List<Long>> loadMeasurableToAppIdsMap(DSLContext dsl,
                                                                   Select<Record1<Long>> inScopeEntityIdSelector,
                                                                   Set<Long> backingEntityReferences,
                                                                   Optional<LocalDate> targetStateDate) {
        return targetStateDate
                .map(targetDate -> loadMeasurableToAppIdsMapUsingTargetState(
                        dsl,
                        inScopeEntityIdSelector,
                        backingEntityReferences,
                        targetDate))
                .orElse(loadMeasurableToAppIdsMapIgnoringTargetDate(
                        dsl,
                        inScopeEntityIdSelector,
                        backingEntityReferences));
    }


    private static Map<Long, List<Long>> loadMeasurableToAppIdsMapUsingTargetState(DSLContext dsl,
                                                                                   Select<Record1<Long>> inScopeEntityIdSelector,
                                                                                   Set<Long> backingEntityReferences,
                                                                                   LocalDate targetStateDate) {
        Date targetDate = targetStateDate == null
                ? null
                : Date.valueOf(targetStateDate);


        Field<Date> decommDate = DSL.coalesce(
                mrpd.PLANNED_DECOMMISSION_DATE,
                app.PLANNED_RETIREMENT_DATE);


        Field<Long> appIdCol = DSL
                .when(decommDate.isNull().or(decommDate.gt(targetDate)),
                        mr.ENTITY_ID)
                .when(mrp.PLANNED_COMMISSION_DATE.lt(targetDate),
                        mrp.ENTITY_ID)
                .otherwise(DSL.castNull(Long.class));

        SelectConditionStep<Record2<Long, Long>> qry = dsl
                .select(mr.MEASURABLE_ID,
                        appIdCol)
                .from(mr)
                .innerJoin(app)
                .on(mr.ENTITY_ID
                        .eq(app.ID)
                        .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(mrpd)
                .on(mr.ID.eq(mrpd.MEASURABLE_RATING_ID))
                .leftJoin(mrp)
                .on(mrp.DECOMMISSION_ID.eq(mrpd.ID))
                .where(mr.ENTITY_ID.in(inScopeEntityIdSelector))
                .and(mr.MEASURABLE_ID.in(backingEntityReferences));

        return qry
                .fetchGroups(
                        mr.MEASURABLE_ID,
                        appIdCol);
    }


    private static Map<Long, List<Long>> loadMeasurableToAppIdsMapIgnoringTargetDate(DSLContext dsl,
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


    private static Map<Long, List<Long>> loadDataTypeToAppIdsMap(DSLContext dsl,
                                                                 Select<Record1<Long>> inScopeEntityIdSelector,
                                                                 Set<Long> backingEntityReferences) {
        return dsl
                .selectDistinct(DATA_TYPE_USAGE.DATA_TYPE_ID, DATA_TYPE_USAGE.ENTITY_ID)
                .from(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(DATA_TYPE_USAGE.ENTITY_ID.in(inScopeEntityIdSelector))
                        .and(DATA_TYPE_USAGE.DATA_TYPE_ID.in(backingEntityReferences)))
                .fetchGroups(DATA_TYPE_USAGE.DATA_TYPE_ID, DATA_TYPE_USAGE.ENTITY_ID);
    }
}
