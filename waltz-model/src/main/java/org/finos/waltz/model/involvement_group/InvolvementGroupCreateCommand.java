package org.finos.waltz.model.involvement_group;

import org.immutables.value.Value;

import java.util.Collections;
import java.util.Set;

@Value.Immutable
public abstract class InvolvementGroupCreateCommand {

    public abstract InvolvementGroup involvementGroup();

    @Value.Default
    public Set<Long> involvementKindIds() {
        return Collections.emptySet();
    }

    ;
}
