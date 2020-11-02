package com.khartec.waltz.model.permission_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePermission.class)
@JsonDeserialize(as = ImmutablePermission.class)
public abstract class Permission {

    public abstract EntityKind subjectKind();
    public abstract EntityKind qualifierKind();
    public abstract Optional<Long> qualifierId();
    public abstract Boolean isDefault();
}
