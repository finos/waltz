package org.finos.waltz.service.maker_checker;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.maker_checker.MakerCheckerPermissionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.utils.Approver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;


@Service
public class MakerCheckerPermissionService {
    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerPermissionService.class);
    private final MakerCheckerPermissionDao makerCheckerPermissionDao;

    @Autowired
    MakerCheckerPermissionService(MakerCheckerPermissionDao makerCheckerPermissionDao){
        checkNotNull(makerCheckerPermissionDao, "changeLogDao cannot be null");
        this.makerCheckerPermissionDao = makerCheckerPermissionDao;
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


}
