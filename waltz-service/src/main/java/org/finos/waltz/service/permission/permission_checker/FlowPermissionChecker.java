/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.permission.permission_checker;

import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.service.user.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.union;


@Service
public class FlowPermissionChecker implements PermissionChecker {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPermissionChecker.class);

    private final LogicalFlowDao logicalFlowDao;
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;
    private final UserRoleService userRoleService;

    @Autowired
    public FlowPermissionChecker(LogicalFlowDao logicalFlowDao,
                                 InvolvementService involvementService,
                                 PermissionGroupService permissionGroupService,
                                 UserRoleService userRoleService) {

        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.userRoleService = userRoleService;
        this.logicalFlowDao = logicalFlowDao;
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return logicalFlowDao.findByEntityReference(ref);
    }


    public Set<Operation> findPermissionsForDecorator(EntityReference entityReference, String username) {
        switch (entityReference.kind()) {
            case LOGICAL_DATA_FLOW:
                return findPermissionsForFlow(entityReference.id(), username);
            case PHYSICAL_SPECIFICATION:
                return findPermissionsForSpec(entityReference.id(), username);
            default:
                throw new UnsupportedOperationException(format("Cannot find decorator permissions for kind: %s", entityReference.kind()));
        }
    }


    public Set<Operation> findPermissionsForSpec(long id, String username) {
        boolean hasOverride = userRoleService.hasRole(username, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        //Do we need to check logicals?

        if (hasOverride) {
            return asSet(Operation.REMOVE, Operation.UPDATE, Operation.ADD);
        } else {
            return emptySet();
        }
    }


    public Set<Operation> findFlowPermissionsForParentEntity(EntityReference entityReference,
                                                             String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.LOGICAL_DATA_FLOW)
                        && p.parentKind().equals(entityReference.kind()))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return logicalFlowDao.calculateAmendedFlowOperations(
                operationsForEntityAssessment,
                entityReference,
                username);
    }


    public Set<Operation> findPermissionsForFlow(Long flowId,
                                                 String username) {

        LogicalFlow flow = logicalFlowDao.getByFlowId(flowId);

        if (flow.isReadOnly()) {
            return emptySet();
        } else {
            return findPermissionsForSourceAndTarget(flow.source(), flow.target(), username);
        }
    }


    public Set<Operation> findPermissionsForSourceAndTarget(EntityReference source,
                                                            EntityReference target,
                                                            String username) {
        Set<Operation> sourcePermissions = findFlowPermissionsForParentEntity(source, username);
        Set<Operation> targetPermissions = findFlowPermissionsForParentEntity(target, username);
        return union(sourcePermissions, targetPermissions);
    }

}
