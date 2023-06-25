package org.finos.waltz.model.measurable_rating;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

/**
 * the purpose of this class is to provide information that can be used
 * to create detail audit change-log messages
 */
@Value.Immutable
public abstract class MeasurableRatingChangeSummary {

    public abstract EntityReference entityRef();

    public abstract EntityReference measurableRef();

    public abstract EntityReference measurableCategoryRef();

    @Nullable
    public abstract Tuple2<String, String> currentRatingNameAndCode();

    @Nullable
    public abstract Tuple2<String, String> desiredRatingNameAndCode();

}
