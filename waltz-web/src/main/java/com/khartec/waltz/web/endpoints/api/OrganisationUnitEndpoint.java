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
