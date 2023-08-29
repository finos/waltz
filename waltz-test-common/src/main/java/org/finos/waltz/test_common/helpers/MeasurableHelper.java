package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingPlannedDecommissionRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.finos.waltz.schema.tables.records.MeasurableRecord;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_PLANNED_DECOMMISSION;

@Service
public class MeasurableHelper {

    protected static final String LAST_UPDATE_USER = "last";
    protected static final String PROVENANCE = "test";

    @Autowired
    private MeasurableCategoryService categoryService;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private DSLContext dsl;


    public long createMeasurableCategory(String name) {
        return createMeasurableCategory(name, null);
    }

    public long createMeasurableCategory(String name, String ratingEditorRole) {
        Set<MeasurableCategory> categories = categoryService.findByExternalId(name);
        return CollectionUtilities
                .maybeFirst(categories)
                .map(c -> c.id().get())
                .orElseGet(() -> {
                    long schemeId = ratingSchemeHelper.createEmptyRatingScheme("test");
                    MeasurableCategoryRecord record = dsl.newRecord(MEASURABLE_CATEGORY);
                    record.setDescription(name);
                    record.setName(name);
                    record.setExternalId(name);
                    record.setRatingSchemeId(schemeId);
                    record.setLastUpdatedBy("admin");
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.setEditable(true);
                    if (ratingEditorRole != null) {
                        record.setRatingEditorRole(ratingEditorRole);
                    }
                    record.store();
                    return record.getId();
                });
    }


    public void updateCategoryNotEditable(long categoryId) {
        dsl
                .update(MEASURABLE_CATEGORY)
                .set(MEASURABLE_CATEGORY.EDITABLE, false)
                .where(MEASURABLE_CATEGORY.ID.eq(categoryId))
                .execute();
    }


    public long createMeasurable(String name, long categoryId) {
        return dsl
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.EXTERNAL_ID.eq(name))
                .and(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetchOptional(MEASURABLE.ID)
                .orElseGet(() -> {
                    MeasurableRecord record = dsl.newRecord(MEASURABLE);
                    record.setMeasurableCategoryId(categoryId);
                    record.setName(name);
                    record.setDescription(name);
                    record.setConcrete(true);
                    record.setExternalId(name);
                    record.setProvenance(PROVENANCE);
                    record.setLastUpdatedBy(LAST_UPDATE_USER);
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.store();
                    return record.getId();
                });
    }


    public long createRating(EntityReference ref, long measurableId) {

        MeasurableRatingRecord ratingRecord = dsl.newRecord(MEASURABLE_RATING);
        ratingRecord.setEntityId(ref.id());
        ratingRecord.setEntityKind(ref.kind().name());
        ratingRecord.setMeasurableId(measurableId);
        ratingRecord.setRating("G");
        ratingRecord.setDescription("test desc");
        ratingRecord.setLastUpdatedAt(nowUtcTimestamp());
        ratingRecord.setLastUpdatedBy("test");
        ratingRecord.setProvenance("test");

        return dsl
                .insertInto(MEASURABLE_RATING)
                .set(ratingRecord)
                .onDuplicateKeyIgnore()
                .returning(MEASURABLE_RATING.ID)
                .fetchOne()
                .getId();
    }


    public long createDecomm(long ratingId) {

        MeasurableRatingPlannedDecommissionRecord decommissionRecord = dsl.newRecord(MEASURABLE_RATING_PLANNED_DECOMMISSION);
        decommissionRecord.setMeasurableRatingId(ratingId);
        decommissionRecord.setPlannedDecommissionDate(toSqlDate(nowUtcTimestamp()));
        decommissionRecord.setCreatedAt(nowUtcTimestamp());
        decommissionRecord.setCreatedBy("test");
        decommissionRecord.setUpdatedAt(nowUtcTimestamp());
        decommissionRecord.setUpdatedBy("test");

        int store = decommissionRecord.store();

        return decommissionRecord.getId();
    }


    public void updateMeasurableReadOnly(EntityReference ref, long measurableId) {
        dsl
                .update(MEASURABLE_RATING)
                .set(MEASURABLE_RATING.IS_READONLY, true)
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name())
                        .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id())
                                .and(MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId))))
                .execute();
    }

}
