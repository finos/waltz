package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.ChangeDirection;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AppChangeEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ApplicationChangeWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAppChangeEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableApplicationChangeWidgetDatum;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.MeasurableRatingPlannedDecommission;
import org.finos.waltz.schema.tables.MeasurableRatingReplacement;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;
import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadMeasurableToAppIdsMap;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.toMeasurableIds;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.COST;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class AppChangesWidgetDao {

    private static final Application a = Application.APPLICATION;
    private static final MeasurableRating mr = MeasurableRating.MEASURABLE_RATING;
    private static final MeasurableRatingPlannedDecommission mrpd = MeasurableRatingPlannedDecommission.MEASURABLE_RATING_PLANNED_DECOMMISSION;
    private static final MeasurableRatingReplacement mrr = MeasurableRatingReplacement.MEASURABLE_RATING_REPLACEMENT;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    mrpd.ENTITY_ID,
                    mrpd.ENTITY_KIND,
                    newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    private final DSLContext dsl;


    @Autowired
    public AppChangesWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<ApplicationChangeWidgetDatum> findWidgetData(long diagramId,
                                                            Select<Record1<Long>> inScopeEntityIdSelector,
                                                            Optional<LocalDate> targetMaxDate) {

        LocalDate startOfYear = DateTimeUtilities.today().withMonth(1).withDayOfMonth(1);

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Set<Long> backingMeasurableEntityIds = toMeasurableIds(cellWithBackingEntities);

        Map<Long, List<Long>> measurableIdToEntityIds = loadMeasurableToAppIdsMap(
                dsl,
                inScopeEntityIdSelector,
                backingMeasurableEntityIds,
                targetMaxDate);

        Map<Long, Tuple3<EntityReference, LocalDate, LocalDate>> appLifecycleById = getAppLifecycleById(inScopeEntityIdSelector);

        Map<Long, List<Tuple2<EntityReference, LocalDate>>> uptakeForMeasurable = fetchReplacementAppInfo(inScopeEntityIdSelector, backingMeasurableEntityIds, targetMaxDate);
        Map<Long, List<Tuple2<EntityReference, LocalDate>>> lossForMeasurable = fetchDecommissionDateInfo(inScopeEntityIdSelector, backingMeasurableEntityIds, targetMaxDate);

        Map<String, Collection<EntityReference>> cellBackingEntitiesByCellExtId = groupBy(
                cellWithBackingEntities,
                t -> t.v1,
                t -> t.v2);

        return cellBackingEntitiesByCellExtId
                .entrySet()
                .stream()
                .map(e -> {
                    Set<EntityReference> value = fromCollection(e.getValue());
                    String cellExtId = e.getKey();

                    Map<EntityKind, Set<Long>> backingEntitiesByKind = value
                            .stream()
                            .collect(groupingBy(
                                    EntityReference::kind,
                                    mapping(EntityReference::id, toSet())));

                    //only supports measurables at the moment
                    Set<Long> measurableIds = backingEntitiesByKind.getOrDefault(EntityKind.MEASURABLE, emptySet());

                    Set<Long> entityIdsViaMeasurable = measurableIds
                            .stream()
                            .flatMap(mID -> measurableIdToEntityIds.getOrDefault(mID, emptyList()).stream())
                            .collect(toSet());

                    Set<AppChangeEntry> appChangeInfo = measurableIds
                            .stream()
                            .flatMap(mID -> {

                                Set<AppChangeEntry> incomingRatingChanges = map(
                                        uptakeForMeasurable.getOrDefault(mID, emptyList()),
                                        r -> mkChangeEntry(r.v1, r.v2, ChangeDirection.INBOUND));

                                Set<AppChangeEntry> outgoingRatingChanges = map(
                                        lossForMeasurable.getOrDefault(mID, emptyList()),
                                        r -> mkChangeEntry(r.v1, r.v2, ChangeDirection.OUTBOUND));

                                Set<AppChangeEntry> appChanges = measurableIdToEntityIds.getOrDefault(mID, emptyList())
                                        .stream()
                                        .flatMap(appId -> {
                                            Tuple3<EntityReference, LocalDate, LocalDate> lifecycleInfo = appLifecycleById.get(appId);

                                            if (lifecycleInfo == null) {
                                                return null;
                                            }

                                            AppChangeEntry retirement = lifecycleInfo.v3 != null
                                                    ? mkChangeEntry(lifecycleInfo.v1, lifecycleInfo.v3, ChangeDirection.OUTBOUND)
                                                    : null;

                                            AppChangeEntry commission = lifecycleInfo.v2 != null
                                                    ? mkChangeEntry(lifecycleInfo.v1, lifecycleInfo.v2, ChangeDirection.INBOUND)
                                                    : null;

                                            return Stream.of(retirement, commission);
                                        })
                                        .filter(Objects::nonNull)
                                        .filter(d -> d.date().isAfter(startOfYear))
                                        .filter(d -> d.date().isBefore(targetMaxDate.get()))
                                        .collect(toSet());

                                return union(incomingRatingChanges, outgoingRatingChanges, appChanges)
                                        .stream()
                                        .collect(toMap(k -> k, v -> v, (v1, v2) -> v1.date().isBefore(v2.date()) ? v1 : v2))
                                        .values()
                                        .stream();
                            })
                            .collect(toSet());


                    return ImmutableApplicationChangeWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .applicationChanges(appChangeInfo)
                            .currentAppCount(entityIdsViaMeasurable.size())
                            .build();
                })
                .filter(d -> d.applicationChanges().size() > 0)
                .collect(toSet());
    }

    private AppChangeEntry mkChangeEntry(EntityReference ref, LocalDate date, ChangeDirection direction) {
        return ImmutableAppChangeEntry.builder()
                .appRef(ref)
                .changeDirection(direction)
                .date(date)
                .build();
    }

    private Map<Long, Tuple3<EntityReference, LocalDate, LocalDate>> getAppLifecycleById(Select<Record1<Long>> inScopeEntityIdSelector) {
        Field<Timestamp> retirementDate = DSL.coalesce(a.PLANNED_RETIREMENT_DATE, a.ACTUAL_RETIREMENT_DATE);

        return dsl
                .select(a.ID,
                        a.NAME,
                        retirementDate,
                        a.COMMISSION_DATE)
                .from(a)
                .where(a.ID.in(inScopeEntityIdSelector))
                .fetchMap(
                        r -> r.get(a.ID),
                        r -> tuple(
                                mkRef(EntityKind.APPLICATION, r.get(a.ID), r.get(a.NAME)),
                                toLocalDate(r.get(a.COMMISSION_DATE)), toLocalDate(r.get(retirementDate))));
    }

    private Map<Long, List<Tuple2<EntityReference, LocalDate>>> fetchDecommissionDateInfo(Select<Record1<Long>> inScopeEntityIdSelector, Set<Long> backingMeasurableEntityIds, Optional<LocalDate> localDate) {

        Condition targetDateCondition = localDate
                .map(targetDate -> mrpd.PLANNED_DECOMMISSION_DATE.le(toSqlDate(targetDate)))
                .orElse(DSL.trueCondition());

        LocalDate startOfYear = DateTimeUtilities.today()
                .withMonth(1)
                .withDayOfMonth(1);

        return dsl
                .select(mrpd.ENTITY_ID,
                        mrpd.MEASURABLE_ID,
                        mrpd.PLANNED_DECOMMISSION_DATE)
                .select(ENTITY_NAME_FIELD)
                .from(mrpd)
                .where(mrpd.ENTITY_ID.in(inScopeEntityIdSelector)
                        .and(mrpd.MEASURABLE_ID.in(backingMeasurableEntityIds))
                        .and(mrpd.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(mrpd.PLANNED_DECOMMISSION_DATE.ge(toSqlDate(startOfYear)))
                        .and(targetDateCondition))
                .fetchGroups(
                        r -> r.get(mrpd.MEASURABLE_ID),
                        r -> tuple(
                                mkRef(EntityKind.APPLICATION, r.get(mrpd.ENTITY_ID), r.get(ENTITY_NAME_FIELD)),
                                toLocalDate(r.get(mrpd.PLANNED_DECOMMISSION_DATE))));
    }

    private Map<Long, List<Tuple2<EntityReference, LocalDate>>> fetchReplacementAppInfo(Select<Record1<Long>> inScopeEntityIdSelector, Set<Long> backingMeasurableEntityIds, Optional<LocalDate> localDate) {

        Condition targetDateCondition = localDate
                .map(targetDate -> mrr.PLANNED_COMMISSION_DATE.le(toSqlDate(targetDate)))
                .orElse(DSL.trueCondition());

        LocalDate startOfYear = DateTimeUtilities.today()
                .withMonth(1)
                .withDayOfMonth(1);

        return dsl
                .select(mrr.ENTITY_ID,
                        mrpd.MEASURABLE_ID,
                        mrr.PLANNED_COMMISSION_DATE)
                .select(ENTITY_NAME_FIELD)
                .from(mrr)
                .innerJoin(mrpd)
                .on(mrr.DECOMMISSION_ID.eq(mrpd.ID))
                .where(mrr.ENTITY_ID.in(inScopeEntityIdSelector)
                        .and(mrr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(mrpd.MEASURABLE_ID.in(backingMeasurableEntityIds))
                        .and(mrr.PLANNED_COMMISSION_DATE.ge(toSqlDate(startOfYear)))
                        .and(targetDateCondition))
                .fetchGroups(
                        r -> r.get(mrpd.MEASURABLE_ID),
                        r -> tuple(
                                mkRef(EntityKind.APPLICATION, r.get(mrpd.ENTITY_ID), r.get(ENTITY_NAME_FIELD)),
                                toLocalDate(r.get(mrr.PLANNED_COMMISSION_DATE))));
    }

}
