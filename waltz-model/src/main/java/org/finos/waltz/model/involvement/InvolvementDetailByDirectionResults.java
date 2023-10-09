package org.finos.waltz.model.involvement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementDetailByDirectionResults.class)
public abstract class InvolvementDetailByDirectionResults {

    public abstract Set<InvolvementDetail> ancestors();
    public abstract Set<InvolvementDetail> descendents();
    public abstract Set<InvolvementDetail> exact();

}
