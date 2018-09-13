package com.khartec.waltz.jobs;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.jooq.DSLContext;

import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;

public class WaltzUtilities {


    public static Long getOrCreateMeasurableCategory(DSLContext dsl, String externalId, String name) {

        Long categoryId = dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(externalId))
                .fetchOne(MEASURABLE_CATEGORY.ID);


        if (categoryId != null) {
            return categoryId;
        } else {
            MeasurableCategoryRecord measurableCategoryRecord = dsl.newRecord(MEASURABLE_CATEGORY);
            measurableCategoryRecord.setName(name);
            measurableCategoryRecord.setDescription(name);
            measurableCategoryRecord.setExternalId(externalId);
            measurableCategoryRecord.setRatingSchemeId(1L);
            measurableCategoryRecord.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
            measurableCategoryRecord.setLastUpdatedBy("admin");
            measurableCategoryRecord.store();
            return measurableCategoryRecord.getId();
        }
    }

}
