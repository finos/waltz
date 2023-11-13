package org.finos.waltz.data.measurable_rating;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.measurable_rating.ImmutablePrimaryRatingViewItem;
import org.finos.waltz.model.measurable_rating.PrimaryRatingViewItem;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class MeasurableRatingViewDao {

    private static final MeasurableRating mr = MEASURABLE_RATING;
    private static final Measurable m = MEASURABLE;
    private static final RatingSchemeItem rsi = RATING_SCHEME_ITEM;
    private static final MeasurableCategory mc = MEASURABLE_CATEGORY;

    private final DSLContext dsl;

    @Autowired
    public MeasurableRatingViewDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    /**
     * Given an application id will return a set of primary ratings.
     * The {@link PrimaryRatingViewItem} is a lightweight object with
     * just enough data for display purposes.
     *
     * @param appId  the identifier of the app to return primary ratings
     * @return  a set of primary ratings, the set will be empty if there are no ratings or the app cannot be found
     */
    public Set<PrimaryRatingViewItem> findPrimaryRatingsForApp(long appId) {
        return dsl
                .select(mc.ID,
                        mc.NAME,
                        mc.DESCRIPTION,
                        m.ID,
                        m.NAME,
                        m.DESCRIPTION,
                        rsi.NAME,
                        rsi.DESCRIPTION,
                        rsi.COLOR)
                .from(mr)
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rsi).on(mr.RATING.eq(rsi.CODE)
                        .and(rsi.SCHEME_ID.eq(mc.RATING_SCHEME_ID)))
                .where(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(mr.ENTITY_ID.eq(appId)))
                .fetchSet(r -> ImmutablePrimaryRatingViewItem
                        .builder()
                        .measurableCategory(mkRef(
                                EntityKind.MEASURABLE_CATEGORY,
                                r.get(mc.ID),
                                r.get(mc.NAME),
                                r.get(mc.DESCRIPTION)))
                        .measurable(mkRef(
                                EntityKind.MEASURABLE,
                                r.get(m.ID),
                                r.get(m.NAME),
                                r.get(m.DESCRIPTION)))
                        .ratingName(r.get(rsi.NAME))
                        .ratingDescription(r.get(rsi.DESCRIPTION))
                        .ratingColor(r.get(rsi.COLOR))
                        .build());
    }
}
