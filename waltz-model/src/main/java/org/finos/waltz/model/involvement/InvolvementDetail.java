package org.finos.waltz.model.involvement;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.involvement_kind.InvolvementKind;
import org.finos.waltz.model.person.Person;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementDetail.class)
@JsonDeserialize(as = ImmutableInvolvementDetail.class)

public abstract class InvolvementDetail {

    public abstract Involvement involvement();
    public abstract Person person();
    public abstract InvolvementKind involvementKind();

}
