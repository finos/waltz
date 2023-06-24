package org.finos.waltz.model.involvement_group;

import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.Set;

@Value.Immutable
public abstract class InvolvementGroupCreateCommand implements Command {

    public abstract InvolvementGroup involvementGroup();

    @Value.Default
    public Set<Long> involvementKindIds() {
        return Collections.emptySet();
    }

}
