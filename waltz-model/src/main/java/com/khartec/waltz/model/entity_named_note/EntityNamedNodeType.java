package com.khartec.waltz.model.entity_named_note;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityNamedNodeType.class)
@JsonDeserialize(as = ImmutableEntityNamedNodeType.class)
public abstract class EntityNamedNodeType implements IdProvider, NameProvider, DescriptionProvider {

    public abstract Set<EntityKind> applicableEntityKinds();
}
