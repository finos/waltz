package org.finos.waltz.model.permission_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

import static org.finos.waltz.common.SetUtilities.hasIntersection;

@Value.Immutable
@JsonSerialize(as = ImmutableRequiredInvolvementsResult.class)
@JsonDeserialize(as = ImmutableRequiredInvolvementsResult.class)
public abstract class RequiredInvolvementsResult {

    public abstract boolean areAllUsersAllowed();


    public abstract Set<Long> requiredInvolvementKindIds();


    public boolean isAllowed(Set<Long> userInvolvementKindIds) {
        return areAllUsersAllowed() ||
                hasIntersection(requiredInvolvementKindIds(), userInvolvementKindIds);
    }
}
