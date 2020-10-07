package com.khartec.waltz.data.report_grid;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.report_grid.*;
import com.khartec.waltz.schema.Tables;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static org.jooq.impl.DSL.name;

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

        SelectConditionStep<Record4<Long, Long, String, Long>> exactMeasurableData = fetchMeasurableData(gridCondition, appSelector);
        SelectConditionStep<Record4<Long, Long, String, Long>> assessmentData = fetchAssessmentData(gridCondition, appSelector);

        SelectOrderByStep<Record4<Long, Long, String, Long>> qry = assessmentData.unionAll(exactMeasurableData);

        return qry
                .fetchSet(r -> ImmutableReportGridRatingCell
                        .builder()
                        .applicationId(r.get(0, Long.class))
                        .columnEntityId(r.get(1, Long.class))
                        .columnEntityKind(EntityKind.valueOf(r.get(2, String.class)))
                        .ratingId(r.get(3, Long.class))
                        .build());

    }


    private SelectConditionStep<Record4<Long, Long, String, Long>> getAggregatedMeasurableData(Condition gridCondition, Select<Record1<Long>> appSelector) {

        SelectConditionStep<Record1<Long>> startingMeasurableIdsForGrid = DSL.select(rgcd.COLUMN_ENTITY_ID)
                .from(rgcd)
                .innerJoin(rg).on(rgcd.REPORT_GRID_ID.eq(rg.ID))
                .where(gridCondition
                        .and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.MEASURABLE.name())
                                .and(rgcd.RATING_ROLLUP_RULE.ne(RatingRollupRule.NONE.name()))));


        SelectConditionStep<Record4<Long, Long, Long, Integer>> selectRatings = dsl.select(
                MEASURABLE.ID,
                    MEASURABLE_RATING.ENTITY_ID,
                    RATING_SCHEME_ITEM.ID,
                    RATING_SCHEME_ITEM.POSITION)
                .from(MEASURABLE)
                .innerJoin(ENTITY_HIERARCHY).on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(MEASURABLE.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(MEASURABLE_RATING).on(MEASURABLE_RATING.MEASURABLE_ID.eq(ENTITY_HIERARCHY.ID))
                .innerJoin(MEASURABLE_CATEGORY).on(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                .innerJoin(RATING_SCHEME_ITEM).on(MEASURABLE_RATING.RATING.eq(RATING_SCHEME_ITEM.CODE)
                        .and(RATING_SCHEME_ITEM.SCHEME_ID.eq(MEASURABLE_CATEGORY.RATING_SCHEME_ID)))
                .where(dsl.renderInlined(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(MEASURABLE.ID.in(startingMeasurableIdsForGrid)
                                .and(MEASURABLE_RATING.ENTITY_ID.in(appSelector)))));

        CommonTableExpression<Record4<Long, Long, Long, Integer>> ratings;
        ratings = name("rating")
                .fields("smid", "eid", "rid", "rp")
                .as(selectRatings);

        Table<Record4<Long, Long, Long, Integer>> r1 = ratings.as("r1");
        Table<Record4<Long, Long, Long, Integer>> r2 = ratings.as("r2");

        SelectConditionStep<Record4<Long, Long, String, Long>> where = dsl
                .with(ratings)
                .selectDistinct(r1.field("eid", Long.class),
                        r1.field("smid", Long.class),
                        DSL.val(EntityKind.MEASURABLE.name()),
                        r1.field("rid", Long.class))
                .from(r1)
                .leftJoin(r2).on(r1.field("eid", Long.class).eq(r2.field("eid", Long.class))
                        .and(r1.field("smid", Long.class).eq(r2.field("smid", Long.class)))
                        .and(r1.field("rp", Integer.class).lt(r2.field("rp", Integer.class))))  // TODO: how to dynamically switch the operator (lt, gt) based on RRR
                .where(dsl.renderInlined(r2.field("rid", Long.class).isNull()
                        .and(r1.field("eid", Long.class).in(appSelector))));
        return where;  // don't use condition as we don't have 'app' table alias
    }


    private SelectConditionStep<Record4<Long, Long, String, Long>> fetchMeasurableData(Condition gridCondition, Select<Record1<Long>> appSelector) {

        Condition measurablesForGridCondition = gridCondition
                .and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(rgcd.RATING_ROLLUP_RULE.eq(RatingRollupRule.NONE.name()));

        SelectConditionStep<Record1<Long>> measurableIdsForGrid = DSL
                .select(rgcd.COLUMN_ENTITY_ID)
                .from(rgcd)
                .innerJoin(rg).on(rgcd.REPORT_GRID_ID.eq(rg.ID))
                .where(dsl.renderInlined(measurablesForGridCondition));

        Condition measurableCondition = mr.ENTITY_ID.in(appSelector)
                .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(mr.MEASURABLE_ID.in(measurableIdsForGrid));

        return dsl
                .select(mr.ENTITY_ID,
                        mr.MEASURABLE_ID,
                        DSL.value(EntityKind.MEASURABLE.name()),
                        rsi.ID)
                .from(mr)
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rsi).on(rsi.CODE.eq(mr.RATING)).and(rsi.SCHEME_ID.eq(mc.RATING_SCHEME_ID))
                .where(dsl.renderInlined(measurableCondition));
    }


    private SelectConditionStep<Record4<Long, Long, String, Long>> fetchAssessmentData(Condition gridCondition, Select<Record1<Long>> appSelector) {
        return dsl
                .select(ar.ENTITY_ID,
                        ar.ASSESSMENT_DEFINITION_ID,
                        DSL.value(EntityKind.ASSESSMENT_DEFINITION.name()),
                        ar.RATING_ID)
                .from(ar)
                .innerJoin(ASSESSMENT_DEFINITION).on(ar.ASSESSMENT_DEFINITION_ID.eq(ASSESSMENT_DEFINITION.ID))
                .innerJoin(rgcd).on(ASSESSMENT_DEFINITION.ID.eq(rgcd.COLUMN_ENTITY_ID)
                        .and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.ASSESSMENT_DEFINITION.name())))
                .innerJoin(rg).on(rgcd.ID.eq(rgcd.REPORT_GRID_ID))
                .where(gridCondition
                        .and(ar.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(ar.ENTITY_ID.in(appSelector)));
    }
}

