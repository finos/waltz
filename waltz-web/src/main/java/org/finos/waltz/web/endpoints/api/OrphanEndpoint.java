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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.service.orphan.OrphanService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.orphan.OrphanRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.requireRole;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.user.SystemRole.ADMIN;

@Service
public class OrphanEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(OrphanEndpoint.class);
    private static final String BASE_URL = mkPath("api", "orphan");

    private final OrphanService orphanService;
    private final UserRoleService userRoleService;


    @Autowired
    public OrphanEndpoint(OrphanService orphanService, UserRoleService userRoleService) {
        checkNotNull(orphanService, "orphanService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.orphanService = orphanService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findApplicationsWithNonExistingOrgUnitPath = mkPath(BASE_URL, "application-non-existing-org-unit");
        String findOrphanMeasurableRatingsPath = mkPath(BASE_URL, "measurable-rating");
        String findOrphanFlowClassificationRulesByAppPath = mkPath(BASE_URL, "flow-classification-rule", "application");
        String findOrphanFlowClassificationRulesByOrgUnitPath = mkPath(BASE_URL, "flow-classification-rule", "org-unit");
        String findOrphanFlowClassificationRulesByDataTypePath = mkPath(BASE_URL, "flow-classification-rule", "data-type");
        String findOrphanChangeInitiativesPath = mkPath(BASE_URL, "change-initiative");
        String findOrphanLogicalDataFlowsPath = mkPath(BASE_URL, "logical-flow");
        String findOrphanPhysicalFlowsPath = mkPath(BASE_URL, "physical-flow");
        String findOrphanAttestationsPath = mkPath(BASE_URL, "attestation");


        ListRoute<OrphanRelationship> findApplicationsWithNonExistingOrgUnitRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findApplicationsWithNonExistingOrgUnit();
        };


        ListRoute<OrphanRelationship> findOrphanMeasurableRatingsRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanMeasurableRatings();
        };


        ListRoute<OrphanRelationship> findOrphanFlowClassificationRulesByAppRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanFlowClassificationRulesByApp();
        };


        ListRoute<OrphanRelationship> findOrphanFlowClassificationRulesByOrgUnitRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanFlowClassificationRulesByOrgUnit();
        };


        ListRoute<OrphanRelationship> findOrphanFlowClassificationRulesByDataTypeRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanFlowClassificationRulesByDataType();
        };


        ListRoute<OrphanRelationship> findOrphanChangeInitiativesRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanChangeInitiatives();
        };


        ListRoute<OrphanRelationship> findOrphanLogicalDataFlowsRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanLogicalDataFlows();
        };


        ListRoute<OrphanRelationship> findOrphanPhysicalFlowsRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanPhysicalFlows();
        };


        ListRoute<OrphanRelationship> findOrphanAttestationsRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanAttestatations();
        };


        getForList(findApplicationsWithNonExistingOrgUnitPath, findApplicationsWithNonExistingOrgUnitRoute);
        getForList(findOrphanMeasurableRatingsPath, findOrphanMeasurableRatingsRoute);
        getForList(findOrphanFlowClassificationRulesByAppPath, findOrphanFlowClassificationRulesByAppRoute);
        getForList(findOrphanFlowClassificationRulesByOrgUnitPath, findOrphanFlowClassificationRulesByOrgUnitRoute);
        getForList(findOrphanFlowClassificationRulesByDataTypePath, findOrphanFlowClassificationRulesByDataTypeRoute);
        getForList(findOrphanChangeInitiativesPath, findOrphanChangeInitiativesRoute);
        getForList(findOrphanLogicalDataFlowsPath, findOrphanLogicalDataFlowsRoute);
        getForList(findOrphanPhysicalFlowsPath, findOrphanPhysicalFlowsRoute);
        getForList(findOrphanAttestationsPath, findOrphanAttestationsRoute);
    }
}
