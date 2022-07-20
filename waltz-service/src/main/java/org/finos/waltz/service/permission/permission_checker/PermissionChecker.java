package org.finos.waltz.service.permission.permission_checker;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;

import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.hasIntersection;

public interface PermissionChecker {

    Set<Operation> EDIT_PERMS = asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE);

    default void verifyAnyPerms(Set<Operation> possiblePerms,
                                Set<Operation> userPerms,
                                EntityKind entityKind,
                                String username) throws InsufficientPrivelegeException {

        if (!hasIntersection(userPerms, possiblePerms)) {
            throw new InsufficientPrivelegeException(format(
                    "%s does not have any of the permissions: %s, for this %s.",
                    username,
                    possiblePerms,
                    entityKind.prettyName()));
        }
    }


    default void verifyEditPerms(Set<Operation> userPerms,
                                 EntityKind entityKind,
                                 String username) throws InsufficientPrivelegeException {

        verifyAnyPerms(EDIT_PERMS, userPerms, entityKind, username);
    }

}
