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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.external_identifier.ExternalIdentifierService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class ExternalIdentifierEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "external-identifier");
    private final ExternalIdentifierService externalIdentifierService;
    private final UserRoleService userRoleService;


    @Autowired
    public ExternalIdentifierEndpoint(ExternalIdentifierService externalIdentifierService,
                                      UserRoleService userRoleService) {
        checkNotNull(externalIdentifierService, "externalIdentifierService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.externalIdentifierService = externalIdentifierService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        ListRoute<ExternalIdentifier> findForEntityReference = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            return externalIdentifierService.findByEntityReference(ref);
        };


        DatumRoute<Integer> deleteRoute = (req, resp) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            EntityReference ref = getEntityReference(req);
            String system = req.params("system");
            String externalId = req.splat()[0];

            return externalIdentifierService.delete(ref, externalId, system, getUsername(req));
        };

        DatumRoute<Integer> createRoute = (req, resp) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            EntityReference ref = getEntityReference(req);
            String externalId = req.splat()[0];

            return externalIdentifierService.create(ref, externalId, getUsername(req));
        };


        // delete
        deleteForDatum(mkPath(BASE_URL, "entity", ":kind", ":id", ":system", "externalId", "*"), deleteRoute);

        postForDatum(mkPath(BASE_URL, "entity", ":kind", ":id", "externalId", "*"), createRoute);

        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), findForEntityReference);
    }

}
