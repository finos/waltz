package org.finos.waltz.model.datatype;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.immutables.value.Value;

@Value.Immutable
public interface FlowDataType {

    EntityReference source();
    EntityReference target();
    @Nullable
    Long sourceOuId();
    @Nullable Long targetOuId();
    long lfId();
    long lfdId();
    long dtId();
    @Nullable Long outboundRuleId();
    @Nullable Long inboundRuleId();
    AuthoritativenessRatingValue sourceOutboundRating();
    AuthoritativenessRatingValue targetInboundRating();
}
