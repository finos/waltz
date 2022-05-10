package org.finos.waltz.model.overlay_diagram;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableBackingEntityCount.class)
public abstract class BackingEntityCount {

    public abstract EntityReference backingEntityReference();
//    public abstract int applicationCount();
}

