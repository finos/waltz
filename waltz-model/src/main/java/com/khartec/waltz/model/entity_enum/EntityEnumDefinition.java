package com.khartec.waltz.model.entity_enum;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityEnumDefinition.class)
@JsonDeserialize(as = ImmutableEntityEnumDefinition.class)
public abstract class EntityEnumDefinition implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        IconProvider {

    public abstract EntityKind entityKind();
    public abstract String enumValueType();
    public abstract int position();
    public abstract boolean isEditable();
}
