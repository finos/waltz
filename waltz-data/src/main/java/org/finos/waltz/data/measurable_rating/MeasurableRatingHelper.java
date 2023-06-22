package org.finos.waltz.data.measurable_rating;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.data.measurable.MeasurableIdSelectorFactory.allMeasurablesIdsInSameCategory;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

public class MeasurableRatingHelper {

    /**
     * Updates the given measurable rating to be set as primary.
     * All other ratings for the same entity/category will be set to non-primary.
     *
     * @param tx           the dsl connection to use
     * @param ref          the entity ref
     * @param measurableId the measurable id
     * @param isPrimary    the new value of the isPrimary flag
     */
    public static boolean saveRatingIsPrimary(DSLContext tx,
                                              EntityReference ref,
                                              long measurableId,
                                              boolean isPrimary,
                                              String username) {
        tx.update(MEASURABLE_RATING)
                .set(MEASURABLE_RATING.IS_PRIMARY, false)
                .where(MEASURABLE_RATING.ENTITY_ID.eq(ref.id())
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                        .and(MEASURABLE_RATING.MEASURABLE_ID.in(allMeasurablesIdsInSameCategory(measurableId))))
                .execute();

        if (isPrimary) {
            // only update if we are setting to true as false case dealt with above
            tx.update(MEASURABLE_RATING)
                    .set(MEASURABLE_RATING.IS_PRIMARY, true)
                    .set(MEASURABLE_RATING.LAST_UPDATED_BY, username)
                    .set(MEASURABLE_RATING.LAST_UPDATED_AT, nowUtcTimestamp())
                    .where(mkPkCondition(ref, measurableId))
                    .execute();
        }

        return true;
    }


    public static boolean saveRatingDescription(DSLContext tx,
                                                EntityReference entityRef,
                                                long measurableId,
                                                String description,
                                                String username) {
        return tx
                .update(MEASURABLE_RATING)
                .set(MEASURABLE_RATING.DESCRIPTION, description)
                .set(MEASURABLE_RATING.LAST_UPDATED_BY, username)
                .set(MEASURABLE_RATING.LAST_UPDATED_AT, nowUtcTimestamp())
                .where(mkPkCondition(entityRef, measurableId))
                .execute() == 1;
    }


    /**
     * @param tx
     * @param entityRef
     * @param measurableId
     * @param ratingCode
     * @param username
     * @return
     */
    public static boolean saveRatingItem(DSLContext tx,
                                         EntityReference entityRef,
                                         long measurableId,
                                         String ratingCode,
                                         String username) {

        boolean exists = doesRatingExist(tx, entityRef, measurableId);

        if (exists) {
            int rc = tx
                    .update(MEASURABLE_RATING)
                    .set(MEASURABLE_RATING.RATING, ratingCode)
                    .set(MEASURABLE_RATING.LAST_UPDATED_BY, username)
                    .set(MEASURABLE_RATING.LAST_UPDATED_AT, nowUtcTimestamp())
                    .where(mkPkCondition(entityRef, measurableId))
                    .execute();
            return rc == 1;
        } else {
            MeasurableRatingRecord r = tx.newRecord(MEASURABLE_RATING);

            r.setRating(ratingCode);
            r.setDescription(null);
            r.setLastUpdatedBy(username);
            r.setLastUpdatedAt(nowUtcTimestamp());
            r.setEntityId(entityRef.id());
            r.setEntityKind(entityRef.kind().name());
            r.setMeasurableId(measurableId);
            r.setProvenance("waltz");

            int rc = r.insert();
            return rc == 1;
        }
    }


    public static boolean doesRatingExist(DSLContext tx,
                                          EntityReference entityRef,
                                          long measurableId) {
        return tx
                .fetchExists(DSL
                        .select(DSL.val(1))
                        .from(MEASURABLE_RATING)
                        .where(mkPkCondition(entityRef, measurableId)));

    }


    private static Condition mkPkCondition(EntityReference entityRef,
                                           long measurableId) {
        return MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId)
                .and(MEASURABLE_RATING.ENTITY_ID.eq(entityRef.id()))
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(entityRef.kind().name()));
    }


}
