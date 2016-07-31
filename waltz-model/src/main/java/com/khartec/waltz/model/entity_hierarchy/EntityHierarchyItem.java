package com.khartec.waltz.model.entity_hierarchy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LevelProvider;
import com.khartec.waltz.model.ParentIdProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableEntityHierarchyItem.class)
@JsonDeserialize(as = ImmutableEntityHierarchyItem.class)
public abstract class EntityHierarchyItem implements
        IdProvider,
        LevelProvider,
        ParentIdProvider
{
    public abstract EntityKind kind();
}
