package com.khartec.waltz.model.roadmap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableRoadmap.class)
@JsonDeserialize(as = ImmutableRoadmap.class)
public abstract class Roadmap implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        EntityLifecycleStatusProvider,
        WaltzEntity {


    public abstract long ratingSchemeId();
    public abstract EntityReference rowType();
    public abstract EntityReference columnType();

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.ROADMAP)
                .id(id().get())
                .name(name())
                .description(Optional.ofNullable(description()))
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
