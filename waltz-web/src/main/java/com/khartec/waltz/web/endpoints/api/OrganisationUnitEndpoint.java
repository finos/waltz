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

import com.khartec.waltz.model.LeveledEntityReference;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class OrganisationUnitEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "org-unit");
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationUnitEndpoint.class);

    private final OrganisationalUnitService service;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public OrganisationUnitEndpoint(OrganisationalUnitService service,
                                    ChangeLogService changeLogService,
                                    UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.service = service;
        this.changeLogService = changeLogService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findAllPath = mkPath(BASE_URL);
        String searchPath = mkPath(BASE_URL, "search", ":query");
        String findByIdsPath = mkPath(BASE_URL, "by-ids");
        String findRelatedByEntityRefPath = mkPath(BASE_URL, "related", ":kind", ":id");
        String getByIdPath = mkPath(BASE_URL, ":id");
        String findDescendantsPath = mkPath(BASE_URL, ":id", "descendants");
        String findImmediateHierarchyPath = mkPath(BASE_URL, ":id", "immediate-hierarchy");

        ListRoute<OrganisationalUnit> findAllRoute = (request, response) -> service.findAll();
        ListRoute<OrganisationalUnit> searchRoute = (request, response) -> service.search(request.params("query"));
        ListRoute<OrganisationalUnit> findByIdsRoute = (req, res) -> service.findByIds(readBody(req, Long[].class));
        ListRoute<OrganisationalUnit> findRelatedByEntityRefRoute = (req, res) -> service.findRelatedByEntityRef(getEntityReference(req));
        ListRoute<LeveledEntityReference> findImmediateHierarchyRoute = (req, res) -> service.findImmediateHierarchy(getId(req));
        ListRoute<OrganisationalUnit> findDescendantsRoute = (req, res) -> service.findDescendants(getId(req));

        DatumRoute<OrganisationalUnit> getByIdRoute = (request, response) -> service.getById(getId(request));


        getForList(findAllPath, findAllRoute);
        getForList(searchPath, searchRoute);
        getForList(findImmediateHierarchyPath, findImmediateHierarchyRoute);
        getForList(findDescendantsPath, findDescendantsRoute);
        postForList(findByIdsPath, findByIdsRoute);
        getForList(findRelatedByEntityRefPath, findRelatedByEntityRefRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }

}
