package com.khartec.waltz.model.external_identifier;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableExternalIdentifier.class)
@JsonDeserialize(as = ImmutableExternalIdentifier.class)
public abstract class ExternalIdentifier implements
        WaltzEntity {

    public abstract String system();
    public abstract String externalId();

}
