package org.finos.waltz.model.permission_group;

import org.immutables.value.Value;

import java.util.Set;

import static org.finos.waltz.common.SetUtilities.hasIntersection;

@Value.Immutable
public abstract class RequiredInvolvementsResult {

    public abstract boolean areAllUsersAllowed();


    public abstract Set<Long> requiredInvolvementKindIds();


    public boolean isAllowed(Set<Long> userInvolvementKindIds) {
        return areAllUsersAllowed() ||
                hasIntersection(requiredInvolvementKindIds(), userInvolvementKindIds);
    }
}
