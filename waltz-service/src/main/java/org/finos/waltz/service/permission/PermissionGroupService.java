package org.finos.waltz.service.permission;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.permission_group.CheckPermissionCommand;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.person.PersonService;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.person.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.finos.waltz.common.SetUtilities.intersection;

@Service
public class PermissionGroupService {
    public static final Set<Long> ALL_USERS_ALLOWED = SetUtilities.asSet((Long) null);
    private static final Logger LOG = LoggerFactory.getLogger(PermissionGroupService.class);

    private final InvolvementService involvementService;
    private final PersonService personService;
    private final PermissionGroupDao permissionGroupDao;

    @Autowired
    public PermissionGroupService(InvolvementService involvementService,
                                  PersonService personService,
                                  PermissionGroupDao permissionGroupDao) {
        this.involvementService = involvementService;
        this.personService = personService;
        this.permissionGroupDao = permissionGroupDao;
    }

    @Deprecated
    public List<Permission> findPermissions(EntityReference parentEntityRef,
                                            String username) {
        Person person = personService.getPersonByUserId(username);

        if (isNull(person)){
            return Collections.emptyList();
        }

        List<Involvement> involvements =
                involvementService.findByEmployeeId(person.employeeId())
                        .stream()
                        .filter(involvement -> involvement.entityReference().equals(parentEntityRef))
                        .collect(Collectors.toList());

        if (involvements.isEmpty()) {
            return Collections.emptyList();
        }

        return permissionGroupDao.getDefaultPermissions();
    }

    @Deprecated
    public boolean hasPermission(EntityReference entityReference,
                                 EntityKind qualifierKind,
                                 String username) {
        return findPermissions(entityReference, username)
                .stream()
                .anyMatch(permission -> permission.qualifierKind().equals(qualifierKind));
    }


    public boolean hasPermission(CheckPermissionCommand permissionCommand) {

        Set<Long> requiredInvolvements = permissionGroupDao.findRequiredInvolvements(permissionCommand);

        if (requiredInvolvements.isEmpty()) {
            // no involvements (incl. defaults) for this requested permission, therefore can safely say 'no'
            return false;
        }

        if (requiredInvolvements.equals(ALL_USERS_ALLOWED)) {
            return true;
        }

        Set<Long> existingInvolvements = permissionGroupDao.findExistingInvolvementKindIdsForUser(permissionCommand);

        System.out.printf("Required perms: %s\n", requiredInvolvements);
        System.out.printf("Existing perms: %s\n", existingInvolvements);

        return ! intersection(requiredInvolvements, existingInvolvements).isEmpty();
    }

}
