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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipKindService;
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

    private final LegalEntityRelationshipKindService legalEntityRelationshipKindService;
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;
    private final UserRoleService userRoleService;

    @Autowired
    public LegalEntityRelationshipPermissionChecker(LegalEntityRelationshipKindService legalEntityRelationshipKindService,
                                                    InvolvementService involvementService,
                                                    PermissionGroupService permissionGroupService,
                                                    UserRoleService userRoleService) {

        checkNotNull(legalEntityRelationshipKindService, "legalEntityRelationshipKindService must not be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.userRoleService = userRoleService;
        this.legalEntityRelationshipKindService = legalEntityRelationshipKindService;
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
    }


    public Set<Operation> findLegalEntityRelationshipPermissionsForRelationshipKind(long relKindId, String username) {
        LegalEntityRelationshipKind relKind = legalEntityRelationshipKindService.getById(relKindId);
        return findLegalEntityRelationshipPermissionsForTargetKind(relKind.targetKind(), username);
    }

    public Set<Operation> findLegalEntityRelationshipPermissionsForTargetKind(EntityKind parentKind,
                                                                              String username) {
        return emptySet();
    }

}
