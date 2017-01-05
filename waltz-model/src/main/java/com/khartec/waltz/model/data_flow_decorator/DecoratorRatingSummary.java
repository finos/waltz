package com.khartec.waltz.model.data_flow_decorator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableDecoratorRatingSummary.class)
@JsonDeserialize(as = ImmutableDecoratorRatingSummary.class)
public abstract class DecoratorRatingSummary {

    public abstract EntityReference decoratorEntityReference();
    public abstract AuthoritativenessRating rating();
    public abstract int count();

}
