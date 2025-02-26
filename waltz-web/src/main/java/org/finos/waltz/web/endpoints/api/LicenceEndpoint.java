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

import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.licence.Licence;
import org.finos.waltz.model.licence.SaveLicenceCommand;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.licence.LicenceService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.getUsername;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.endpoints.EndpointUtilities.deleteForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class LicenceEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LicenceEndpoint.class);
    private static final String BASE_URL = mkPath("api", "licence");

    private final LicenceService service;

    private final UserRoleService userRoleService;


    @Autowired
    public LicenceEndpoint(LicenceService service,
                           UserRoleService userRoleService) {
        checkNotNull(service, "service cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // read
        getForList(mkPath(BASE_URL, "all"), (request, response) -> service.findAll());
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute );
        getForDatum(mkPath(BASE_URL, "external-id", ":externalId"), this::getByExternalIdRoute );
        getForList(mkPath(BASE_URL, "count", "application"), (request, response) -> service.countApplications());
        postForList(mkPath(BASE_URL, "selector"), this::findBySelectorRoute);
        postForDatum(mkPath(BASE_URL, "save"), this::saveRoute);
        deleteForDatum(mkPath(BASE_URL, "id", ":id"), this::removeRoute);
    }


    private Licence getByExternalIdRoute(Request request, Response response) {
        String externalId = request.params("externalId");
        return service.getByExternalId(externalId);
    }


    private Licence getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }


    private  List<Licence> findBySelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions options = WebUtilities.readIdSelectionOptionsFromBody(request);
        return service.findBySelector(options);
    }

    private boolean saveRoute(Request request, Response response) throws IOException {
        SaveLicenceCommand cmd = readBody(request, SaveLicenceCommand.class);
        String username = getUsername(request);
        checkHasEditPermissions(username);
        return service.save(cmd, username);
    }

    private boolean removeRoute(Request request, Response response) throws IOException {
        long licenceId = getId(request);
        String username = getUsername(request);
        checkHasEditPermissions(username);
        return service.remove(licenceId, getUsername(request));
    }

    private void checkHasEditPermissions(String username) {
        checkTrue(
                userRoleService.hasRole(username, SystemRole.LICENCE_ADMIN),
                "User does not have the required permissions to edit licences");
    }
}
