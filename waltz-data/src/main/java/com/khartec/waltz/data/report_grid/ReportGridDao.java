package com.khartec.waltz.data.report_grid;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.report_grid.ImmutableReportGridDefinition;
import com.khartec.waltz.model.report_grid.ImmutableReportGridRatingCell;
import com.khartec.waltz.model.report_grid.ReportGridDefinition;
import com.khartec.waltz.model.report_grid.ReportGridRatingCell;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.*;
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

@Repository
public class ReportGridDao {

    private final DSLContext dsl;

    private final Application app = APPLICATION.as("app");
    private final Measurable m = MEASURABLE.as("m");
    private final MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private final MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
    private final ReportGridColumnDefinition rgcd = REPORT_GRID_COLUMN_DEFINITION.as("rgcd");
    private final ReportGrid rg = Tables.REPORT_GRID.as("rg");
    private final RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");
    private final AssessmentDefinition ad = ASSESSMENT_DEFINITION.as("ad");
    private final AssessmentRating ar = ASSESSMENT_RATING.as("ar");


    @Autowired
    public ReportGridDao(DSLContext dsl) {
        this.dsl = dsl;
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
                    .columnEntityReferences(getColumns(condition))
                    .build());
    }


    private List<EntityReference> getColumns(Condition condition) {
        SelectConditionStep<Record5<String, Long, String, String, Integer>> assessmentDefinitionColumns = dsl
                .select(DSL.coalesce(rgcd.DISPLAY_NAME, ad.NAME).as("name"),
                        rgcd.COLUMN_ENTITY_ID,
                        rgcd.COLUMN_ENTITY_KIND,
                        ad.DESCRIPTION.as("desc"),
                        rgcd.POSITION.as("pos"))
                .from(rgcd)
                .innerJoin(ad).on(ad.ID.eq(rgcd.COLUMN_ENTITY_ID).and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.ASSESSMENT_DEFINITION.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .where(condition);

        SelectConditionStep<Record5<String, Long, String, String, Integer>> measurableColumns = dsl
                .select(DSL.coalesce(rgcd.DISPLAY_NAME, m.NAME).as("name"),
                        rgcd.COLUMN_ENTITY_ID,
                        rgcd.COLUMN_ENTITY_KIND,
                        m.DESCRIPTION.as("desc"),
                        rgcd.POSITION.as("pos"))
                .from(rgcd)
                .innerJoin(m).on(m.ID.eq(rgcd.COLUMN_ENTITY_ID).and(rgcd.COLUMN_ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(rg).on(rg.ID.eq(rgcd.REPORT_GRID_ID))
                .where(condition);

        List<EntityReference> columns = assessmentDefinitionColumns
                .unionAll(measurableColumns)
                .orderBy(DSL.field("pos"), DSL.field("name"))
                .fetch(r -> mkRef(
                        EntityKind.valueOf(r.get(rgcd.COLUMN_ENTITY_KIND)),
                        r.get(rgcd.COLUMN_ENTITY_ID),
                        r.get("name", String.class),
                        r.get("desc", String.class)));
        return columns;
    }


    private Set<ReportGridRatingCell> findCellDataByGridCondition(Condition gridCondition,
                                                                  Select<Record1<Long>> appSelector) {

        Condition condition = app.ID.in(appSelector)
                .and(gridCondition);

        SelectConditionStep<Record4<Long, Long, String, Long>> measurableData = dsl
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
                .where(dsl.renderInlined(condition)); // jOOQ seems very slow when using simple `condition` (esp. for complex conditions). Inlined seems a lot faster

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

        return measurableData
                .unionAll(assessmentData)
                .fetchSet(r -> ImmutableReportGridRatingCell
                        .builder()
                        .applicationId(r.get(app.ID))
                        .columnEntityId(r.get(rgcd.COLUMN_ENTITY_ID))
                        .columnEntityKind(EntityKind.valueOf(r.get("kind", String.class)))
                        .ratingId(r.get(rsi.ID))
                        .build());
    }


}

