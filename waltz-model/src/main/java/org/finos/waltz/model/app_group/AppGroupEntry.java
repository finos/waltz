package org.finos.waltz.model.app_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IsReadOnlyProvider;
import org.finos.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAppGroupEntry.class)
@JsonDeserialize(as = ImmutableAppGroupEntry.class)
public abstract class AppGroupEntry extends EntityReference implements
        ProvenanceProvider,
        IsReadOnlyProvider {
}
