package org.finos.waltz.service.maker_checker;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.maker_checker.MakerCheckerPermissionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.utils.Approver;
import org.finos.waltz.model.utils.Checker;
import org.finos.waltz.model.utils.ImmutableChecker;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;


@Service
public class MakerCheckerPermissionService {
    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerPermissionService.class);
    private final MakerCheckerPermissionDao makerCheckerPermissionDao;
    private final InvolvementService involvementService;

    private final PermissionGroupService permissionGroupService;

    @Autowired
    MakerCheckerPermissionService(MakerCheckerPermissionDao makerCheckerPermissionDao,
                                  InvolvementService involvementService,
                                  PermissionGroupService permissionGroupService){
        checkNotNull(makerCheckerPermissionDao, "changeLogDao cannot be null");
        this.makerCheckerPermissionDao = makerCheckerPermissionDao;
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
    }
    public Set<Approver> checkPermission(String username, EntityReference sourceEntity, EntityReference targetEntity) throws InsufficientPrivelegeException {
        boolean isSourceApprover = validateUserPermission(username, sourceEntity.id(), sourceEntity.kind().name());
        boolean isTargetApprover = validateUserPermission(username, targetEntity.id(), targetEntity.kind().name());
        if(isSourceApprover && isTargetApprover){
            return asSet(Approver.SOURCE_APPROVER, Approver.TARGET_APPROVER);
        } else if (isSourceApprover) {
            return asSet(Approver.SOURCE_APPROVER);
        }else if(isTargetApprover) {
            return asSet(Approver.TARGET_APPROVER);
        }else{
            throw new InsufficientPrivelegeException("Insufficient Privileges");
        }
    }
    public boolean validateUserPermission(String username, Long entityKindId, String entityKind) {
        List<Long> involvementKindIds;
        if (entityKind.equals(EntityKind.APPLICATION.name())) {

            involvementKindIds = makerCheckerPermissionDao.getInvolvementKindIdsFromInvolvementGroupEntry("Flow Approvers");

        } else if (entityKind.equals(EntityKind.END_USER_APPLICATION.name())) {

            involvementKindIds = makerCheckerPermissionDao.getIdsFromInvolvementKind("Asset Owner", EntityKind.END_USER_APPLICATION.name());

        }else if (entityKind.equals(EntityKind.ACTOR.name())) {

            involvementKindIds = makerCheckerPermissionDao.getIdsFromInvolvementKind("Data Office Member", EntityKind.ACTOR.name());

        }else{
            throw new IllegalArgumentException(format("Cannot find lookup map for entity reference for entity kind: %s", entityKind));
        }
        return makerCheckerPermissionDao.getByEntityReferenceAndWorkflowId(username, entityKindId, involvementKindIds);
    }

    public Checker checkUserPermission(String username, EntityReference sourceEntityReference , EntityReference targetEntityReference) {
        Set<Operation> sourceOperationSet = validatePermission(username, sourceEntityReference);
        Set<Operation> targetOperationSet = validatePermission(username, targetEntityReference);
        return ImmutableChecker.builder()
                .sourceApprover(sourceOperationSet)
                .targetApprover(targetOperationSet)
                .build();
    }

    public Set<Operation> validatePermission(String username,EntityReference entityReference ){
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
