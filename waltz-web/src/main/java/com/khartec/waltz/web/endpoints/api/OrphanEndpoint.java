/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.orphan.OrphanRelationship;
import com.khartec.waltz.service.orphan.OrphanService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.user.Role.ADMIN;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.requireRole;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

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
        String findOrphanAuthoritativeSourcesByAppPath = mkPath(BASE_URL, "authoritative-source", "application");
        String findOrphanAuthoritativeSourcesByOrgUnitPath = mkPath(BASE_URL, "authoritative-source", "org-unit");
        String findOrphanAuthoritativeSourcesByDataTypePath = mkPath(BASE_URL, "authoritative-source", "data-type");
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


        ListRoute<OrphanRelationship> findOrphanAuthoritativeSourcesByAppRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanAuthoritativeSourceByApp();
        };


        ListRoute<OrphanRelationship> findOrphanAuthoritativeSourcesByOrgUnitRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanAuthoritativeSourceByOrgUnit();
        };


        ListRoute<OrphanRelationship> findOrphanAuthoritativeSourcesByDataTypeRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findOrphanAuthoritiveSourceByDataType();
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
        getForList(findOrphanAuthoritativeSourcesByAppPath, findOrphanAuthoritativeSourcesByAppRoute);
        getForList(findOrphanAuthoritativeSourcesByOrgUnitPath, findOrphanAuthoritativeSourcesByOrgUnitRoute);
        getForList(findOrphanAuthoritativeSourcesByDataTypePath, findOrphanAuthoritativeSourcesByDataTypeRoute);
        getForList(findOrphanChangeInitiativesPath, findOrphanChangeInitiativesRoute);
        getForList(findOrphanLogicalDataFlowsPath, findOrphanLogicalDataFlowsRoute);
        getForList(findOrphanPhysicalFlowsPath, findOrphanPhysicalFlowsRoute);
        getForList(findOrphanAttestationsPath, findOrphanAttestationsRoute);
    }
}
