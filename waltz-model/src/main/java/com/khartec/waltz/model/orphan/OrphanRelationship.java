package com.khartec.waltz.model.orphan;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableOrphanRelationship.class)
@JsonDeserialize(as = ImmutableOrphanRelationship.class)
public abstract class OrphanRelationship {

    public abstract EntityReference entityA();
    public abstract EntityReference entityB();
    public abstract OrphanSide orphanSide();

}
