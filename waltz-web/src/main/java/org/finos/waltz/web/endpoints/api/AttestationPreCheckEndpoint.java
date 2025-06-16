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


import org.finos.waltz.model.EntityReference;
import org.finos.waltz.service.attestation.AttestationPreCheckService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class AttestationPreCheckEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "attestation-pre-check");

    private final AttestationPreCheckService attestationPreCheckService;


    @Autowired
    public AttestationPreCheckEndpoint(AttestationPreCheckService attestationPreCheckService) {
        this.attestationPreCheckService = checkNotNull(attestationPreCheckService, "attestationPreCheckService must not be null");
    }


    @Override
    public void register() {
        String logicalFlowCheckPath = WebUtilities.mkPath(BASE_URL, "logical-flow", "entity", ":kind", ":id");
        String viewpointCheckPath = WebUtilities.mkPath(BASE_URL, "viewpoint", "entity", ":kind", ":id", "category", ":categoryId");

        ListRoute<String> logicalFlowCheckRoute =
                (req, res) -> attestationPreCheckService.calcLogicalFlowPreCheckFailures(WebUtilities.getEntityReference(req));


        ListRoute<String> viewpointFlowCheckRoute =
                (req, res) -> {
                    EntityReference entityReference = WebUtilities.getEntityReference(req);
                    long attestedEntityId = WebUtilities.getLong(req, "categoryId");
                    return attestationPreCheckService.calcCapabilitiesPreCheckFailures(entityReference, attestedEntityId);
                };

        EndpointUtilities.getForList(logicalFlowCheckPath, logicalFlowCheckRoute);
        EndpointUtilities.getForList(viewpointCheckPath, viewpointFlowCheckRoute);
    }
}
