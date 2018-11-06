package com.khartec.waltz.model.roadmap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRoadmapCreateCommand.class)
@JsonDeserialize(as = ImmutableRoadmapCreateCommand.class)
public abstract class RoadmapCreateCommand implements Command, NameProvider {

    public abstract long ratingSchemeId();
    public abstract EntityReference rowType();
    public abstract EntityReference columnType();
    public abstract EntityReference linkedEntity();

}
