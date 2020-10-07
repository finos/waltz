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
import static org.jooq.impl.DSL.select;

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

        Condition condition = app.ID.in(appSelector)
                .and(gridCondition);

        SelectConditionStep<Record4<Long, Long, String, Long>> exactMeasurableData = dsl
                .select(app.ID,
                        rgcd.COLUMN_ENTITY_ID,
                        DSL.value(EntityKind.MEASURABLE.name()).as("kind"),
                        rsi.ID)
                .from(mr)
                .innerJoin(app).on(app.ID.eq(mr.ENTITY_ID)).and(mr.ENTITY_KIND.eq(DSL.val(EntityKind.APPLICATION.name())))
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rgcd).on(rgcd.COLUMN_ENTITY_ID.eq(m.ID)).and(rgcd.COLUMN_ENTITY_KIND.eq(DSL.val(EntityKind.MEASURABLE.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .innerJoin(rsi).on(rsi.CODE.eq(mr.RATING)).and(rsi.SCHEME_ID.eq(mc.RATING_SCHEME_ID))
                .where(dsl.renderInlined(condition))
                .and(rgcd.DISPLAY_NAME.ne("agg")); // jOOQ seems very slow when using simple `condition` (esp. for complex conditions). Inlined seems a lot faster

        SelectConditionStep<Record4<Long, Long, String, Long>> assessmentData = dsl
                .select(app.ID,
                        rgcd.COLUMN_ENTITY_ID,
                        DSL.value(EntityKind.ASSESSMENT_DEFINITION.name()).as("kind"),
                        rsi.ID)
                .from(ar)
                .innerJoin(app).on(app.ID.eq(ar.ENTITY_ID)).and(ar.ENTITY_KIND.eq(DSL.val(EntityKind.APPLICATION.name())))
                .innerJoin(ad).on(ad.ID.eq(ar.ASSESSMENT_DEFINITION_ID))
                .innerJoin(rgcd).on(rgcd.COLUMN_ENTITY_ID.eq(ad.ID)).and(rgcd.COLUMN_ENTITY_KIND.eq(DSL.val(EntityKind.ASSESSMENT_DEFINITION.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .innerJoin(rsi).on(rsi.ID.eq(ar.RATING_ID))
                .where(dsl.renderInlined(condition));  // jOOQ seems very slow when using simple `condition` (esp. for complex conditions). Inlined seems a lot faster

        SelectConditionStep<Record4<Long, Long, Long, Integer>> selectRatings = select(
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
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        CommonTableExpression<Record4<Long, Long, Long, Integer>> ratings = name("rating")
                .fields("smid", "eid", "rid", "rp")
                .as(selectRatings);

        Table<Record4<Long, Long, Long, Integer>> r1 = ratings.as("r1");
        Table<Record4<Long, Long, Long, Integer>> r2 = ratings.as("r2");

        SelectConditionStep<Record4<Long, Long, String, Long>> aggregatedMeasurableData = dsl
                .with(ratings)
                .selectDistinct(
                        r1.field("eid", Long.class),
                        r1.field("smid", Long.class),
                        DSL.val(EntityKind.MEASURABLE.name()),
                        r1.field("rid", Long.class))
                .from(r1)
                .leftJoin(r2).on(r1.field("eid", Long.class).eq(r2.field("eid", Long.class))
                        .and(r1.field("smid", Long.class).eq(r2.field("smid", Long.class))
                                .and(r1.field("rp", Integer.class).gt(r2.field("rp", Integer.class)))))
                .innerJoin(rgcd).on(rgcd.COLUMN_ENTITY_ID.eq(r1.field("smid", Long.class))
                        .and(rgcd.COLUMN_ENTITY_KIND.eq(DSL.val(EntityKind.MEASURABLE.name()))
                                .and(rgcd.DISPLAY_NAME.eq("agg"))))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .where(r2.field("rid", Long.class).isNull()
                        .and(r1.field("eid", Long.class).in(appSelector)));

        return aggregatedMeasurableData
                .unionAll(assessmentData)
                .unionAll(exactMeasurableData)
                .fetchSet(r -> ImmutableReportGridRatingCell
                        .builder()
                        .applicationId(r.get(0, Long.class))
                        .columnEntityId(r.get(1, Long.class))
                        .columnEntityKind(EntityKind.valueOf(r.get(2, String.class)))
                        .ratingId(r.get(3, Long.class))
                        .build());
    }
}

