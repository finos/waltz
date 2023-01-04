package org.finos.waltz.model.assessment_rating;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonDeserialize(as = ImmutableSummaryCountRequest.class)
public abstract class SummaryCountRequest {

    public abstract IdSelectionOptions idSelectionOptions();

    public abstract Set<Long> definitionIds();
}
