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
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.service.user.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class LegalEntityRelationshipPermissionChecker implements PermissionChecker {

    private static final Logger LOG = LoggerFactory.getLogger(LegalEntityRelationshipPermissionChecker.class);

    private final LogicalFlowDao logicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;
    private final UserRoleService userRoleService;

    @Autowired
    public LegalEntityRelationshipPermissionChecker(LogicalFlowDao logicalFlowDao,
                                                    PhysicalSpecificationDao physicalSpecificationDao,
                                                    InvolvementService involvementService,
                                                    PermissionGroupService permissionGroupService,
                                                    UserRoleService userRoleService) {

        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao must not be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.userRoleService = userRoleService;
        this.logicalFlowDao = logicalFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
    }


    public Set<Operation> findLegalEntityRelationshipPermissionsForParentKind(EntityKind parentKind,
                                                                              String username) {

        return emptySet();

//        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);
//
//        Set<Operation> operationsForEntityAssessment = permissionGroupService
//                .findPermissionsForParentReference(entityReference, username)
//                .stream()
//                .filter(p -> p.subjectKind().equals(EntityKind.LOGICAL_DATA_FLOW)
//                        && p.parentKind().equals(entityReference.kind()))
//                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
//                .map(Permission::operation)
//                .collect(Collectors.toSet());
//
//        return logicalFlowDao.calculateAmendedFlowOperations(
//                operationsForEntityAssessment,
//                username);
    }

}
