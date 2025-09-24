package org.finos.waltz.service.proposed_flow_workflow;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.proposed_flow.ImmutableProposeFlowPermission;
import org.finos.waltz.model.proposed_flow.ProposeFlowPermission;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProposedFlowWorkflowPermissionService {
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowWorkflowPermissionService.class);
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;

    @Autowired
    ProposedFlowWorkflowPermissionService(InvolvementService involvementService,
                                          PermissionGroupService permissionGroupService) {
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
    }

    public ProposeFlowPermission checkUserPermission(String username, EntityReference sourceEntityReference, EntityReference targetEntityReference) {
        Set<Operation> sourceOperationSet = fetchPermittedOperationsForUser(username, sourceEntityReference);
        LOG.debug("For user {}, permitted operations are: {} for source {}",
                username, sourceOperationSet, sourceEntityReference);
        Set<Operation> targetOperationSet = fetchPermittedOperationsForUser(username, targetEntityReference);
        LOG.debug("For user {}, permitted operations are: {} for target {}",
                username, targetOperationSet, targetEntityReference);
        return ImmutableProposeFlowPermission.builder()
                .sourceApprover(sourceOperationSet)
                .targetApprover(targetOperationSet)
                .build();
    }

    private Set<Operation> fetchPermittedOperationsForUser(String username, EntityReference entityReference) {
        Set<Long> listOfInvKinds = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);
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
