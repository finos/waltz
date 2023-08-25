package org.finos.waltz.service.permission.permission_checker;

import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.EntityReferenceUtilities;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class AllocationPermissionChecker implements PermissionChecker {

    private final MeasurableRatingDao measurableRatingDao;
    private final PermissionGroupService permissionGroupService;
    private final InvolvementService involvementService;

    @Autowired
    public AllocationPermissionChecker(MeasurableRatingDao measurableRatingDao,
                                       PermissionGroupService permissionGroupService,
                                       InvolvementService involvementService) {

        checkNotNull(measurableRatingDao, "measurableRatingDao cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");

        this.permissionGroupService = permissionGroupService;
        this.measurableRatingDao = measurableRatingDao;
        this.involvementService = involvementService;
    }

    public Set<Operation> findAllocationPermissions(EntityReference entityReference,
                                                    long categoryId,
                                                    String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.MEASURABLE_RATING)
                        && p.parentKind().equals(entityReference.kind())
                        && EntityReferenceUtilities.sameRef(p.qualifierReference(), mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId)))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return measurableRatingDao.calculateAmendedAllocationOperations(
                operationsForEntityAssessment,
                categoryId,
                username);
    }
}
