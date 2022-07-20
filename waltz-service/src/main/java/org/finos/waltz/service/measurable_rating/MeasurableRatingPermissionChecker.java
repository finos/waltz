package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.EntityReferenceUtilities;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;

public class MeasurableRatingPermissionChecker {

    private final MeasurableRatingService measurableRatingService;
    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableService measurableService;
    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final PermissionGroupService permissionGroupService;
    private final InvolvementService involvementService;
    private final UserRoleService userRoleService;

    @Autowired
    public MeasurableRatingPermissionChecker(MeasurableRatingService measurableRatingService,
                                             MeasurableRatingDao measurableRatingDao,
                                             MeasurableService measurableService,
                                             MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                             MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                             PermissionGroupService permissionGroupService,
                                             InvolvementService involvementService,
                                             UserRoleService userRoleService) {

        checkNotNull(measurableRatingService, "measurableRatingService must not be null");
        checkNotNull(measurableRatingPlannedDecommissionService, "measurableRatingPlannedDecommissionService cannot be null");
        checkNotNull(measurableRatingDao, "measurableRatingDao cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.userRoleService = userRoleService;
        this.measurableRatingService = measurableRatingService;
        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.measurableService = measurableService;
        this.permissionGroupService = permissionGroupService;
        this.measurableRatingDao = measurableRatingDao;
        this.involvementService = involvementService;
    }

    public Set<Operation> findMeasurableRatingDecommPermissions(EntityReference entityReference,
                                                                long measurableId,
                                                                String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);
        Measurable measurable = measurableService.getById(measurableId);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION)
                        && p.parentKind().equals(entityReference.kind())
                        && EntityReferenceUtilities.sameRef(p.qualifierReference(), mkRef(EntityKind.MEASURABLE_CATEGORY, measurable.categoryId())))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return measurableRatingPlannedDecommissionDao.calculateAmendedDecommOperations(
                operationsForEntityAssessment,
                measurable.categoryId(),
                username);
    }

}
