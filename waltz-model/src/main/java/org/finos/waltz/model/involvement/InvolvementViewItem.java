package org.finos.waltz.model.involvement;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.person.Person;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementViewItem.class)
@JsonDeserialize(as = ImmutableInvolvementViewItem.class)

public abstract class InvolvementViewItem {

    public abstract Involvement involvement();
    public abstract Person person();

}
