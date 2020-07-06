package com.khartec.waltz.model.app_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IsReadOnlyProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAppGroupEntry.class)
@JsonDeserialize(as = ImmutableAppGroupEntry.class)
public abstract class AppGroupEntry extends EntityReference implements ProvenanceProvider, IsReadOnlyProvider {
}
