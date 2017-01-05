package com.khartec.waltz.model.authoritativesource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableAuthoritativeRatingVantagePoint.class)
@JsonDeserialize(as = ImmutableAuthoritativeRatingVantagePoint.class)
public abstract class AuthoritativeRatingVantagePoint {

    public abstract EntityReference vantagePoint();
    public abstract int rank();
    public abstract String dataTypeCode();
    public abstract Long applicationId();
    public abstract AuthoritativenessRating rating();

}