package com.khartec.waltz.model.application_capability;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableSaveAppCapabilityCommand.class)
@JsonDeserialize(as = ImmutableSaveAppCapabilityCommand.class)
public abstract class SaveAppCapabilityCommand implements Command, DescriptionProvider {

    public abstract boolean isNew();
    public abstract long capabilityId();
    public abstract RagRating rating();

}
