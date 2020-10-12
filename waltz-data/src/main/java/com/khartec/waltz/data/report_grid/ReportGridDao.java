package com.khartec.waltz.data.report_grid;


import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.report_grid.*;
import com.khartec.waltz.schema.Tables;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.SetUtilities.union;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static java.util.Collections.emptySet;

@Repository
public class ReportGridDao {

    private final DSLContext dsl;

    private final com.khartec.waltz.schema.tables.Application app = APPLICATION.as("app");
    private final com.khartec.waltz.schema.tables.Measurable m = MEASURABLE.as("m");
    private final com.khartec.waltz.schema.tables.MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private final com.khartec.waltz.schema.tables.MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
    private final com.khartec.waltz.schema.tables.ReportGridColumnDefinition rgcd = REPORT_GRID_COLUMN_DEFINITION.as("rgcd");
    private final com.khartec.waltz.schema.tables.ReportGrid rg = Tables.REPORT_GRID.as("rg");
    private final com.khartec.waltz.schema.tables.RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");
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
        SelectConditionStep<Record6<String, Long, String, String, Integer, String>> assessmentDefinitionColumns = dsl
                .select(DSL.coalesce(rgcd.DISPLAY_NAME, ad.NAME).as("name"),
                        rgcd.COLUMN_ENTITY_ID,
                        rgcd.COLUMN_ENTITY_KIND,
                        ad.DESCRIPTION.as("desc"),
                        rgcd.POSITION,
                        rgcd.COLUMN_USAGE_KIND)
                .from(rgcd)
                .innerJoin(ad).on(ad.ID.eq(rgcd.COLUMN_ENTITY_ID).and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.ASSESSMENT_DEFINITION.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .where(condition);

        SelectConditionStep<Record6<String, Long, String, String, Integer, String>> measurableColumns = dsl
                .select(DSL.coalesce(rgcd.DISPLAY_NAME, m.NAME).as("name"),
                        rgcd.COLUMN_ENTITY_ID,
                        rgcd.COLUMN_ENTITY_KIND,
                        m.DESCRIPTION.as("desc"),
                        rgcd.POSITION,
                        rgcd.COLUMN_USAGE_KIND)
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

        return union(
            fetchAssessmentData(appSelector, requiredAssessmentDefinitions),
            fetchExactMeasurableData(appSelector, exactMeasurableIds));
    }


    private Set<ReportGridRatingCell> fetchExactMeasurableData(Select<Record1<Long>> appSelector,
                                                               Set<Long> exactMeasurableIds) {
        Table<Record1<Long>> apps = appSelector.asTable();

        SelectConditionStep<Record3<Long, Long, Long>> qry = dsl
                .select(mr.ENTITY_ID,
                        mr.MEASURABLE_ID,
                        rsi.ID)
                .from(mr)
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rsi).on(rsi.CODE.eq(mr.RATING)).and(rsi.SCHEME_ID.eq(mc.RATING_SCHEME_ID))
                .innerJoin(apps).on(apps.field(0, Long.class).eq(mr.ENTITY_ID)
                        .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(mr.MEASURABLE_ID.in(exactMeasurableIds));

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

