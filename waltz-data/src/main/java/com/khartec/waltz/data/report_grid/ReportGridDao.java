package com.khartec.waltz.data.report_grid;


import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.report_grid.*;
import com.khartec.waltz.schema.Tables;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.SetUtilities.union;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static java.util.Collections.emptySet;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class ReportGridDao {

    private final DSLContext dsl;

    private final com.khartec.waltz.schema.tables.Measurable m = MEASURABLE.as("m");
    private final com.khartec.waltz.schema.tables.MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private final com.khartec.waltz.schema.tables.MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
    private final com.khartec.waltz.schema.tables.ReportGridColumnDefinition rgcd = REPORT_GRID_COLUMN_DEFINITION.as("rgcd");
    private final com.khartec.waltz.schema.tables.ReportGrid rg = Tables.REPORT_GRID.as("rg");
    private final com.khartec.waltz.schema.tables.RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");
    private final com.khartec.waltz.schema.tables.EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
    private final com.khartec.waltz.schema.tables.AssessmentDefinition ad = ASSESSMENT_DEFINITION.as("ad");
    private final com.khartec.waltz.schema.tables.AssessmentRating ar = ASSESSMENT_RATING.as("ar");


    @Autowired
    public ReportGridDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<ReportGridDefinition> findAll(){
        return dsl
                .selectFrom(rg)
                .fetchSet(r -> ImmutableReportGridDefinition
                        .builder()
                        .id(r.get(rg.ID))
                        .name(r.get(rg.NAME))
                        .description(r.get(rg.DESCRIPTION))
                        .externalId(Optional.ofNullable(r.get(rg.EXTERNAL_ID)))
                        .provenance(r.get(rg.PROVENANCE))
                        .lastUpdatedAt(toLocalDateTime(r.get(rg.LAST_UPDATED_AT)))
                        .lastUpdatedBy(r.get(rg.LAST_UPDATED_BY))
                        .columnDefinitions(getColumns(rgcd.REPORT_GRID_ID.eq(r.get(rg.ID))))
                        .build());
    }


    public Set<ReportGridRatingCell> findCellDataByGridId(long id,
                                                          Select<Record1<Long>> appSelector) {
        return findCellDataByGridCondition(rg.ID.eq(id), appSelector);
    }


    public Set<ReportGridRatingCell> findCellDataByGridExternalId(String  externalId,
                                                                  Select<Record1<Long>> appSelector) {
        return findCellDataByGridCondition(rg.EXTERNAL_ID.eq(externalId), appSelector);
    }


    public ReportGridDefinition getGridDefinitionById(long id) {
        return getGridDefinitionByCondition(rg.ID.eq(id));
    }


    public ReportGridDefinition getGridDefinitionByExternalId(String externalId) {
        return getGridDefinitionByCondition(rg.EXTERNAL_ID.eq(externalId));
    }


    // --- Helpers ---

    private ReportGridDefinition getGridDefinitionByCondition(Condition condition) {
        return dsl
                .selectFrom(rg)
                .where(condition)
                .fetchOne(r -> ImmutableReportGridDefinition
                    .builder()
                    .id(r.get(rg.ID))
                    .name(r.get(rg.NAME))
                    .description(r.get(rg.DESCRIPTION))
                    .externalId(Optional.ofNullable(r.get(rg.EXTERNAL_ID)))
                    .provenance(r.get(rg.PROVENANCE))
                    .lastUpdatedAt(toLocalDateTime(r.get(rg.LAST_UPDATED_AT)))
                    .lastUpdatedBy(r.get(rg.LAST_UPDATED_BY))
                    .columnDefinitions(getColumns(condition))
                    .build());
    }


    private List<ReportGridColumnDefinition> getColumns(Condition condition) {
        SelectConditionStep<Record7<String, Long, String, String, Integer, String, String>> assessmentDefinitionColumns = dsl
                .select(DSL.coalesce(rgcd.DISPLAY_NAME, ad.NAME).as("name"),
                        rgcd.COLUMN_ENTITY_ID,
                        rgcd.COLUMN_ENTITY_KIND,
                        ad.DESCRIPTION.as("desc"),
                        rgcd.POSITION,
                        rgcd.COLUMN_USAGE_KIND,
                        rgcd.RATING_ROLLUP_RULE)
                .from(rgcd)
                .innerJoin(ad).on(ad.ID.eq(rgcd.COLUMN_ENTITY_ID).and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.ASSESSMENT_DEFINITION.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .where(condition);

        SelectConditionStep<Record7<String, Long, String, String, Integer, String, String>> measurableColumns = dsl
                .select(DSL.coalesce(rgcd.DISPLAY_NAME, m.NAME).as("name"),
                        rgcd.COLUMN_ENTITY_ID,
                        rgcd.COLUMN_ENTITY_KIND,
                        m.DESCRIPTION.as("desc"),
                        rgcd.POSITION,
                        rgcd.COLUMN_USAGE_KIND,
                        rgcd.RATING_ROLLUP_RULE)
                .from(rgcd)
                .innerJoin(m).on(m.ID.eq(rgcd.COLUMN_ENTITY_ID).and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .where(condition);

        return assessmentDefinitionColumns
                .unionAll(measurableColumns)
                .orderBy(rgcd.POSITION, DSL.field("name"))
                .fetch(r -> ImmutableReportGridColumnDefinition.builder()
                        .columnEntityReference(mkRef(
                                EntityKind.valueOf(r.get(rgcd.COLUMN_ENTITY_KIND)),
                                r.get(rgcd.COLUMN_ENTITY_ID),
                                r.get("name", String.class),
                                r.get("desc", String.class)))
                        .position(r.get(rgcd.POSITION))
                        .usageKind(ColumnUsageKind.valueOf(r.get(rgcd.COLUMN_USAGE_KIND)))
                        .ratingRollupRule(RatingRollupRule.valueOf(r.get(rgcd.RATING_ROLLUP_RULE)))
                        .build());
    }


    private Set<ReportGridRatingCell> findCellDataByGridCondition(Condition gridCondition,
                                                                  Select<Record1<Long>> appSelector) {

        ReportGridDefinition gridDefn = getGridDefinitionByCondition(gridCondition);

        Map<EntityKind, Collection<ReportGridColumnDefinition>> colsByKind = groupBy(
                gridDefn.columnDefinitions(),
                cd -> cd.columnEntityReference().kind());

        Set<Long> requiredAssessmentDefinitions = SetUtilities.map(
                colsByKind.getOrDefault(EntityKind.ASSESSMENT_DEFINITION, emptySet()),
                cd -> cd.columnEntityReference().id());

        Map<RatingRollupRule, Collection<ReportGridColumnDefinition>> measurableColumnsByRollupKind = groupBy(
                colsByKind.getOrDefault(EntityKind.MEASURABLE, emptySet()),
                ReportGridColumnDefinition::ratingRollupRule);

        Set<Long> exactMeasurableIds = SetUtilities.map(
                measurableColumnsByRollupKind.get(RatingRollupRule.NONE),
                cd -> cd.columnEntityReference().id());

        Set<Long> summaryMeasurableIdsUsingHighest = SetUtilities.map(
                measurableColumnsByRollupKind.get(RatingRollupRule.PICK_HIGHEST),
                cd -> cd.columnEntityReference().id());

        Set<Long> summaryMeasurableIdsUsingLowest = SetUtilities.map(
                measurableColumnsByRollupKind.get(RatingRollupRule.PICK_LOWEST),
                cd -> cd.columnEntityReference().id());

        return union(
            fetchSummaryMeasurableData(appSelector, summaryMeasurableIdsUsingHighest, summaryMeasurableIdsUsingLowest),
            fetchAssessmentData(appSelector, requiredAssessmentDefinitions),
            fetchExactMeasurableData(appSelector, exactMeasurableIds));
    }


    private Set<ReportGridRatingCell> fetchSummaryMeasurableData(Select<Record1<Long>> appSelector,
                                                                 Set<Long> measurableIdsUsingHighest,
                                                                 Set<Long> measurableIdsUsingLowest) {

        if (measurableIdsUsingHighest.size() == 0 && measurableIdsUsingLowest.size() == 0){
            return emptySet();
        }

        Table<Record5<Long, String, Long, Integer, String>> ratingSchemeItems = DSL
                .select(mc.ID.as("mcId"),
                        rsi.CODE.as("rsiCode"),
                        rsi.ID.as("rsiId"),
                        rsi.POSITION.as("rsiPos"),
                        rsi.NAME.as("rsiName"))
                .from(mc)
                .innerJoin(rsi)
                .on(rsi.SCHEME_ID.eq(mc.RATING_SCHEME_ID))
                .asTable("ratingSchemeItems");

        SelectConditionStep<Record5<Long, Long, Long, Integer, String>> ratings = DSL
                .select(
                        m.ID,
                        mr.ENTITY_ID,
                        ratingSchemeItems.field("rsiId", Long.class),
                        ratingSchemeItems.field("rsiPos", Integer.class),
                        ratingSchemeItems.field("rsiName", String.class))
                .from(m)
                .innerJoin(eh)
                    .on(eh.ANCESTOR_ID.eq(m.ID))
                    .and(eh.KIND.eq(EntityKind.MEASURABLE.name()))
                .innerJoin(mr)
                    .on(mr.MEASURABLE_ID.eq(eh.ID))
                .innerJoin(ratingSchemeItems)
                    .on(m.MEASURABLE_CATEGORY_ID.eq(ratingSchemeItems.field("mcId", Long.class)))
                    .and(mr.RATING.eq(ratingSchemeItems.field("rsiCode", String.class)))
                .where(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(mr.ENTITY_ID.in(appSelector))
                .and(m.ID.in(union(measurableIdsUsingHighest, measurableIdsUsingLowest)));

        return dsl
                .resultQuery(dsl.renderInlined(ratings))
                .fetchGroups(
                        r -> tuple(
                                mkRef(EntityKind.APPLICATION, r.get(mr.ENTITY_ID)),
                                r.get(m.ID)),
                        r -> tuple(
                                r.get("rsiId", Long.class),
                                r.get("rsiPos", Integer.class),
                                r.get("rsiName", String.class)))
                .entrySet()
                .stream()
                .map(e -> {
                    Tuple2<EntityReference, Long> entityAndMeasurable = e.getKey();
                    Long measurableId = entityAndMeasurable.v2();
                    long applicationId = entityAndMeasurable.v1().id();
                    List<Tuple3<Long, Integer, String>> ratingsForEntityAndMeasurable = e.getValue();

                    ToIntFunction<Tuple3<Long, Integer, String>> compareByPositionAsc = t -> t.v2;
                    ToIntFunction<Tuple3<Long, Integer, String>> compareByPositionDesc = t -> t.v2 * -1;
                    Function<? super Tuple3<Long, Integer, String>, String> compareByName = t -> t.v3;

                    Comparator<Tuple3<Long, Integer, String>> cmp = Comparator
                            .comparingInt(measurableIdsUsingHighest.contains(measurableId)
                                    ? compareByPositionAsc
                                    : compareByPositionDesc)
                            .thenComparing(compareByName);

                    return ratingsForEntityAndMeasurable
                            .stream()
                            .min(cmp)
                            .map(t -> ImmutableReportGridRatingCell
                                    .builder()
                                    .applicationId(applicationId)
                                    .columnEntityId(measurableId)
                                    .columnEntityKind(EntityKind.MEASURABLE)
                                    .ratingId(t.v1)
                                    .build())
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    private Set<ReportGridRatingCell> fetchExactMeasurableData(Select<Record1<Long>> appSelector,
                                                               Set<Long> exactMeasurableIds) {

        if (exactMeasurableIds.size() == 0) {
            return emptySet();
        }

        SelectConditionStep<Record3<Long, Long, Long>> qry = dsl
                .select(mr.ENTITY_ID,
                        mr.MEASURABLE_ID,
                        rsi.ID)
                .from(mr)
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rsi).on(rsi.CODE.eq(mr.RATING)).and(rsi.SCHEME_ID.eq(mc.RATING_SCHEME_ID))
                .where(mr.MEASURABLE_ID.in(exactMeasurableIds))
                .and(mr.ENTITY_ID.in(appSelector))
                .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return qry
                .fetchSet(r -> ImmutableReportGridRatingCell.builder()
                        .applicationId(r.get(mr.ENTITY_ID))
                        .columnEntityId(r.get(mr.MEASURABLE_ID))
                        .columnEntityKind(EntityKind.MEASURABLE)
                        .ratingId(r.get(rsi.ID))
                        .build());
    }


    private Set<ReportGridRatingCell> fetchAssessmentData(Select<Record1<Long>> appSelector,
                                                          Set<Long> requiredAssessmentDefinitionIds) {
        if (requiredAssessmentDefinitionIds.size() == 0) {
            return emptySet();
        } else {
            return dsl
                    .select(ar.ENTITY_ID,
                            ar.ASSESSMENT_DEFINITION_ID,
                            ar.RATING_ID)
                    .from(ar)
                    .where(ar.ASSESSMENT_DEFINITION_ID.in(requiredAssessmentDefinitionIds)
                            .and(ar.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                            .and(ar.ENTITY_ID.in(appSelector)))
                    .fetchSet(r -> ImmutableReportGridRatingCell.builder()
                            .applicationId(r.get(ar.ENTITY_ID))
                            .columnEntityId(r.get(ar.ASSESSMENT_DEFINITION_ID))
                            .columnEntityKind(EntityKind.ASSESSMENT_DEFINITION)
                            .ratingId(r.get(ar.RATING_ID))
                            .build());
        }
    }
}

