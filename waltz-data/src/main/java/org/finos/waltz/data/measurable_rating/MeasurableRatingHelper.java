package org.finos.waltz.data.measurable_rating;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingChangeSummary;
import org.finos.waltz.model.measurable_rating.MeasurableRatingChangeSummary;
import org.finos.waltz.schema.tables.*;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;

import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.data.measurable.MeasurableIdSelectorFactory.allMeasurablesIdsInSameCategory;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;

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
     * @param tx  the dsl context to use
     * @param entityRef  the entity the rating is against, usually an app
     * @param measurableId  the measurable being rated against the entity
     * @param ratingCode  the rating code to use
     * @param username  who is doing the rating
     * @return  true iff the item was saved
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


    /**
     * Given and entity, a measurable and a desired rating code this will
     * return an object with the following:
     *
     * <ul>
     *     <li>Resolved measurable name and id</li>
     *     <li>Resolved measurable category name and id</li>
     *     <li>Maybe a tuple of any current mapping name and code</li>
     *     <li>Maybe a tuple of the desired mapping name and code</li>
     * </ul>
     *
     * The main use of this method is to provide additional logging context
     *
     * @param tx dslContext to issue the query with
     * @param entityRef  the entity which needs the new rating
     * @param measurableId  the measurable id for the new rating
     * @param desiredRatingCode  the rating code for the new rating
     * @return  resolved names
     */
    public static MeasurableRatingChangeSummary resolveLoggingContextForRatingChange(DSLContext tx,
                                                                                     EntityReference entityRef,
                                                                                     long measurableId,
                                                                                     String desiredRatingCode) {

        Measurable m = MEASURABLE.as("m");
        MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
        org.finos.waltz.schema.tables.MeasurableRating mr = MEASURABLE_RATING.as("mr");
        RatingScheme rs = RATING_SCHEME.as("rs");
        RatingSchemeItem current = RATING_SCHEME_ITEM.as("current");
        RatingSchemeItem desired = RATING_SCHEME_ITEM.as("desired");
        Application app = APPLICATION.as("app");

        SelectConditionStep<Record> qry = tx
                .select(m.NAME, m.ID)
                .select(mc.NAME, mc.ID)
                .select(app.NAME, app.ID)
                .select(current.NAME, current.CODE)
                .select(desired.NAME, desired.CODE)
                .from(m)
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rs).on(rs.ID.eq(mc.RATING_SCHEME_ID))
                .innerJoin(app).on(app.ID.eq(entityRef.id()))
                .leftJoin(mr).on(mr.MEASURABLE_ID.eq(m.ID)
                                .and(mr.ENTITY_ID.eq(entityRef.id()))
                                .and(mr.ENTITY_KIND.eq(entityRef.kind().name())))
                .leftJoin(current).on(current.CODE.eq(mr.RATING)
                                .and(current.SCHEME_ID.eq(rs.ID)))
                .leftJoin(desired).on(desired.CODE.eq(desiredRatingCode)
                                .and(desired.SCHEME_ID.eq(rs.ID)))
                .where(m.ID.eq(measurableId));

        return qry
                .fetchOne(r -> {
                    String currentCode = r.get(current.CODE);
                    String currentName = r.get(current.NAME);
                    String desiredCode = r.get(desired.CODE);
                    String desiredName = r.get(desired.NAME);

                    Tuple2<String, String> currentRatingNameAndCode = currentCode == null
                            ? null
                            : tuple(currentName, currentCode);

                    Tuple2<String, String> desiredRatingNameAndCode = desiredCode == null
                            ? null
                            : tuple(desiredName, desiredCode);

                    return ImmutableMeasurableRatingChangeSummary
                            .builder()
                            .measurableRef(mkRef(EntityKind.MEASURABLE, measurableId, r.get(m.NAME)))
                            .measurableCategoryRef(mkRef(EntityKind.MEASURABLE_CATEGORY, r.get(mc.ID), r.get(mc.NAME)))
                            .entityRef(ImmutableEntityReference.copyOf(entityRef).withName(r.get(app.NAME)))
                            .currentRatingNameAndCode(currentRatingNameAndCode)
                            .desiredRatingNameAndCode(desiredRatingNameAndCode)
                            .build();
                });

    }

}
