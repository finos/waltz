package com.khartec.waltz.model.invovement_kind;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementKindCreateCommand.class)
@JsonDeserialize(as = ImmutableInvolvementKindCreateCommand.class)
public abstract class InvolvementKindCreateCommand implements NameProvider, DescriptionProvider {

}
