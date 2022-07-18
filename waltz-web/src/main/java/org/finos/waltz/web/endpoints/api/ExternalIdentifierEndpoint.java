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

import org.finos.waltz.model.Operation;
import org.finos.waltz.service.external_identifier.ExternalIdentifierService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.external_identifier.ExternalIdentifier;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.CollectionUtilities.notEmpty;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.intersection;
import static org.finos.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static org.finos.waltz.web.WebUtilities.getUsername;


@Service
public class ExternalIdentifierEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "external-identifier");
    private final ExternalIdentifierService externalIdentifierService;
    private final UserRoleService userRoleService;
    private final LogicalFlowService logicalFlowService;


    @Autowired
    public ExternalIdentifierEndpoint(ExternalIdentifierService externalIdentifierService,
                                      UserRoleService userRoleService,
                                      LogicalFlowService logicalFlowService) {
        checkNotNull(externalIdentifierService, "externalIdentifierService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");

        this.externalIdentifierService = externalIdentifierService;
        this.logicalFlowService = logicalFlowService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        ListRoute<ExternalIdentifier> findForEntityReference = (req, resp) -> {
            EntityReference ref = WebUtilities.getEntityReference(req);
            return externalIdentifierService.findByEntityReference(ref);
        };


        DatumRoute<Integer> deleteRoute = (req, resp) -> {
            EntityReference ref = WebUtilities.getEntityReference(req);
            String system = req.params("system");
            String externalId = req.splat()[0];

            checkHasPermission(ref, getUsername(req));

            return externalIdentifierService.delete(ref, externalId, system, getUsername(req));
        };

        DatumRoute<Integer> createRoute = (req, resp) -> {
            EntityReference ref = WebUtilities.getEntityReference(req);
            String externalId = req.splat()[0];
            checkHasPermission(ref, getUsername(req));
            return externalIdentifierService.create(ref, externalId, getUsername(req));
        };


        // delete
        EndpointUtilities.deleteForDatum(WebUtilities.mkPath(BASE_URL, "entity", ":kind", ":id", ":system", "externalId", "*"), deleteRoute);

        EndpointUtilities.postForDatum(WebUtilities.mkPath(BASE_URL, "entity", ":kind", ":id", "externalId", "*"), createRoute);

        EndpointUtilities.getForList(WebUtilities.mkPath(BASE_URL, "entity", ":kind", ":id"), findForEntityReference);
    }


    private void checkHasPermission(EntityReference ref, String username) {

        if (ref.kind().equals(LOGICAL_DATA_FLOW)) {

            Set<Operation> permissions = logicalFlowService.findPermissionsForFlow(ref.id(), username);
            Set<Operation> editPermissions = intersection(permissions, asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));

            checkTrue(
                    notEmpty(editPermissions),
                    "User does not have permission to edit the external identifier for this flow");
        }
    }
}
