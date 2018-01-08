package com.khartec.waltz.model.entity_event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityEvent.class)
@JsonDeserialize(as = ImmutableEntityEvent.class)
public abstract class EntityEvent implements
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract EntityReference entityReference();
    public abstract String eventType();
    public abstract LocalDateTime eventDate();
}
