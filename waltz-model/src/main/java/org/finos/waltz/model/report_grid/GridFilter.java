package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableGridFilter.class)
@JsonDeserialize(as = ImmutableGridFilter.class)
public abstract class GridFilter {

    public abstract Long columnDefinitionId();

    public abstract Set<String> optionCodes();

}
