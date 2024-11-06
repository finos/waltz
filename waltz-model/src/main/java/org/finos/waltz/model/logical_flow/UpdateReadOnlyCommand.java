package org.finos.waltz.model.logical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateReadOnlyCommand.class)
@JsonDeserialize(as = ImmutableUpdateReadOnlyCommand.class)
public interface UpdateReadOnlyCommand {
    boolean readOnly();
}
