package org.finos.waltz.service.permission.permission_checker;

import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import org.finos.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.EntityReferenceUtilities;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class MeasurableRatingPermissionChecker implements PermissionChecker {

    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableService measurableService;
    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final MeasurableRatingReplacementDao measurableRatingReplacementDao;
    private final PermissionGroupService permissionGroupService;
    private final InvolvementService involvementService;

    @Autowired
    public MeasurableRatingPermissionChecker(MeasurableRatingDao measurableRatingDao,
                                             MeasurableService measurableService,
                                             MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                             MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                             MeasurableRatingReplacementDao measurableRatingReplacementDao,
                                             PermissionGroupService permissionGroupService,
                                             InvolvementService involvementService) {

        checkNotNull(measurableRatingPlannedDecommissionService, "measurableRatingPlannedDecommissionService cannot be null");
        checkNotNull(measurableRatingDao, "measurableRatingDao cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(measurableRatingReplacementDao, "measurableRatingReplacementDao cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");

        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.measurableRatingReplacementDao = measurableRatingReplacementDao;
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


    public Set<Operation> findMeasurableRatingPermissions(EntityReference entityReference,
                                                          long measurableId,
                                                          String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);
        Measurable measurable = measurableService.getById(measurableId);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.MEASURABLE_RATING)
                        && p.parentKind().equals(entityReference.kind())
                        && EntityReferenceUtilities.sameRef(p.qualifierReference(), mkRef(EntityKind.MEASURABLE_CATEGORY, measurable.categoryId())))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return measurableRatingDao.calculateAmendedRatingOperations(
                operationsForEntityAssessment,
                entityReference,
                measurableId,
                username);
    }


    public Set<Operation> findMeasurableRatingReplacementPermissions(Long decommId,
                                                                     String username) {

        MeasurableRatingPlannedDecommission decomm = measurableRatingPlannedDecommissionService.getById(decommId);

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(decomm.entityReference(), username);
        Measurable measurable = measurableService.getById(decomm.measurableId());

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(decomm.entityReference(), username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.MEASURABLE_RATING_REPLACEMENT)
                        && p.parentKind().equals(decomm.entityReference().kind())
                        && EntityReferenceUtilities.sameRef(p.qualifierReference(), mkRef(EntityKind.MEASURABLE_CATEGORY, measurable.categoryId())))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return measurableRatingReplacementDao.calculateAmendedReplacementOperations(
                operationsForEntityAssessment,
                measurable.categoryId(),
                username);
    }

}
