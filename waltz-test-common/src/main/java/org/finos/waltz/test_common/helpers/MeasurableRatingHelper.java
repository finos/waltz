package org.finos.waltz.test_common.helpers;

import org.finos.waltz.data.measurable_rating.MeasurableRatingUtilities;
import org.finos.waltz.model.EntityReference;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasurableRatingHelper {

    @Autowired
    private DSLContext dsl;

    public boolean saveRatingItem(EntityReference entityRef,
                                  long measurableId,
                                  String ratingCode,
                                  String username) {

        return MeasurableRatingUtilities.saveRatingItem(
                dsl,
                entityRef,
                measurableId,
                ratingCode,
                username);
    }
}
