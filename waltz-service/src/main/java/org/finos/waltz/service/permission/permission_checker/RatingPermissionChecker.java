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

import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.service.user.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;


@Service
public class RatingPermissionChecker implements PermissionChecker {

    private static final Logger LOG = LoggerFactory.getLogger(RatingPermissionChecker.class);

    private final AssessmentRatingDao assessmentRatingDao;
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;
    private final UserRoleService userRoleService;

    @Autowired
    public RatingPermissionChecker(AssessmentRatingDao assessmentRatingDao,
                                   InvolvementService involvementService,
                                   PermissionGroupService permissionGroupService,
                                   UserRoleService userRoleService) {

        checkNotNull(assessmentRatingDao, "assessmentRatingDao must not be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.userRoleService = userRoleService;
        this.assessmentRatingDao = assessmentRatingDao;
        this.permissionGroupService = permissionGroupService;
        this.involvementService = involvementService;
    }


    public Set<Operation> findRatingPermissions(EntityReference entityReference,
                                                long assessmentDefinitionId,
                                                String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.ASSESSMENT_RATING)
                        && p.parentKind().equals(entityReference.kind())
                        && p.qualifierReference()
                        .map(ref -> mkRef(EntityKind.ASSESSMENT_DEFINITION, assessmentDefinitionId).equals(ref))
                        .orElse(false))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return assessmentRatingDao.calculateAmendedRatingOperations(
                operationsForEntityAssessment,
                entityReference,
                assessmentDefinitionId,
                username);
    }

}
