package org.finos.waltz.model.bulk_upload;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableResolveRowResponse.class)
public abstract class ResolveRowResponse {

    public abstract List<String> inputRow();

    public abstract ResolutionStatus status();

    public abstract Optional<String> errorMessage();
}
