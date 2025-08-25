package org.finos.waltz.service.maker_checker;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.utils.Checker;
import org.finos.waltz.model.utils.ImmutableChecker;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class MakerCheckerPermissionService {
    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerPermissionService.class);
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;

    @Autowired
    MakerCheckerPermissionService(InvolvementService involvementService,
                                  PermissionGroupService permissionGroupService){
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
    }

    public Checker checkUserPermission(String username, EntityReference sourceEntityReference , EntityReference targetEntityReference) {
        Set<Operation> sourceOperationSet = validatePermission(username, sourceEntityReference);
        LOG.info("sourceOperationSet : {}", sourceOperationSet);
        Set<Operation> targetOperationSet = validatePermission(username, targetEntityReference);
        LOG.info("targetOperationSet : {}", targetOperationSet);
        return ImmutableChecker.builder()
                .sourceApprover(sourceOperationSet)
                .targetApprover(targetOperationSet)
                .build();
    }

    public Set<Operation> validatePermission(String username,EntityReference entityReference ){
        Set<Long> listOfInvKinds = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);
        LOG.info("listOfInvKinds : {}", listOfInvKinds);
        return permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.PROPOSED_FLOW)
                        && p.parentKind().equals(entityReference.kind()))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(listOfInvKinds))
                .map(Permission::operation)
                .collect(Collectors.toSet());
    }


}
