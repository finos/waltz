package com.khartec.waltz.model.invovement_kind;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.EntityChangeCommand;
import com.khartec.waltz.model.command.FieldChange;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableInvolvementKindChangeCommand.class)
@JsonDeserialize(as = ImmutableInvolvementKindChangeCommand.class)
public abstract class InvolvementKindChangeCommand implements EntityChangeCommand {

    public abstract Optional<FieldChange<String>> name();

    public abstract Optional<FieldChange<String>> description();

 }
