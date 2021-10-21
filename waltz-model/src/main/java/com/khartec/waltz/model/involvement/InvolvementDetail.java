package com.khartec.waltz.model.involvement;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.involvement_kind.InvolvementKind;
import com.khartec.waltz.model.person.Person;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementDetail.class)
@JsonDeserialize(as = ImmutableInvolvementDetail.class)

public abstract class InvolvementDetail {

    public abstract Involvement involvement();
    public abstract Person person();
    public abstract InvolvementKind involvementKind();

}
