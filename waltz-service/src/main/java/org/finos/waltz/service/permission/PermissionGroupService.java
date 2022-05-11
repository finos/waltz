package org.finos.waltz.service.permission;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.permission_group.CheckPermissionCommand;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.permission_group.RequiredInvolvementsResult;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.person.PersonService;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.finos.waltz.common.SetUtilities.filter;

@Service
public class PermissionGroupService {
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
    public Set<Permission> findPermissions(EntityReference parentEntityRef,
                                           String username) {
        Person person = personService.getPersonByUserId(username);

        if (isNull(person)) {
            return Collections.emptySet();
        }

        List<Involvement> involvements =
                involvementService.findByEmployeeId(person.employeeId())
                        .stream()
                        .filter(involvement -> involvement.entityReference().equals(parentEntityRef))
                        .collect(Collectors.toList());

        if (involvements.isEmpty()) {
            return Collections.emptySet();
        }

        return permissionGroupDao.getDefaultPermissions();
    }


    public Set<Permission> findPermissionsForSubjectKind(EntityReference parentEntityRef,
                                                         EntityKind subjectKind,
                                                         String username) {

        Person person = personService.getPersonByUserId(username);

        if (isNull(person)) {
            return Collections.emptySet();
        }

        Set<Permission> permissions = permissionGroupDao.findPermissionsForEntityRefAndSubjectKind(parentEntityRef, subjectKind);

        Set<Long> involvements = permissionGroupDao.findExistingInvolvementKindIdsForUser(parentEntityRef, username);

        return filter(permissions, p -> p.requiredInvolvementsResult().isAllowed(involvements));
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

        RequiredInvolvementsResult required = permissionGroupDao.getRequiredInvolvements(permissionCommand);

        if (required.areAllUsersAllowed()) {
            return true;
        }

        if (required.requiredInvolvementKindIds().isEmpty()) {
            // no involvements (incl. defaults) for this requested permission, therefore can safely say 'no'
            return false;
        }

        Set<Long> existingInvolvements = permissionGroupDao.findExistingInvolvementKindIdsForUser(
                permissionCommand.parentEntityRef(),
                permissionCommand.user());

        return required.isAllowed(existingInvolvements);
    }


    public Set<Tuple2<MeasurableCategory, Boolean>> findSupportedMeasurableCategoryAttestations(EntityReference ref, String userId) {
        return permissionGroupDao.findSupportedMeasurableCategoryAttestations(ref, userId);
    }
}
