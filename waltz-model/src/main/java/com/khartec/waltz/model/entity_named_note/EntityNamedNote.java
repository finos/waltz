package com.khartec.waltz.model.entity_named_note;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityNamedNote.class)
@JsonDeserialize(as = ImmutableEntityNamedNote.class)
public abstract class EntityNamedNote implements ProvenanceProvider, LastUpdatedProvider {

    public abstract EntityReference entityReference();
    public abstract long namedNoteTypeId();
    public abstract String noteText();
}
