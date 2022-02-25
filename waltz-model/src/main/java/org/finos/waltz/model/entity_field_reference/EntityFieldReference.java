package org.finos.waltz.model.entity_field_reference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityKindProvider;
import org.finos.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityFieldReference.class)
@JsonDeserialize(as = ImmutableEntityFieldReference.class)
public abstract class EntityFieldReference implements IdProvider, EntityKindProvider {

    public abstract EntityKind entityKind();

    public abstract String fieldName();

    public abstract String displayName();

    public abstract String description();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.ENTITY_FIELD_REFERENCE;
    }

}
