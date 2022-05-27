package org.finos.waltz.model.permission_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePermission.class)
@JsonDeserialize(as = ImmutablePermission.class)
public abstract class Permission {

    public abstract Operation operation();

    public abstract EntityKind parentKind();

    public abstract EntityKind subjectKind();

    public abstract Optional<EntityReference> qualifierReference();

    public abstract RequiredInvolvementsResult requiredInvolvementsResult();
}
