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


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdCommandResponse;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.attestation.AttestationCreateSummary;
import com.khartec.waltz.model.attestation.AttestationRun;
import com.khartec.waltz.model.attestation.AttestationRunCreateCommand;
import com.khartec.waltz.model.attestation.AttestationRunResponseSummary;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.attestation.AttestationRunService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class AttestationRunEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "attestation-run");

    private final AttestationRunService attestationRunService;
    private final UserRoleService userRoleService;


    @Autowired
    public AttestationRunEndpoint(AttestationRunService attestationRunService,
                                  UserRoleService userRoleService) {
        checkNotNull(attestationRunService, "attestationRunService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.attestationRunService = attestationRunService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findAllPath = mkPath(BASE_URL);
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByRecipientPath = mkPath(BASE_URL, "user");
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String findResponseSummariesPath = mkPath(BASE_URL, "summary", "response");
        String getCreateSummaryPath = mkPath(BASE_URL, "create-summary");


        DatumRoute<AttestationRun> getByIdRoute = (req, res) ->
                attestationRunService.getById(getId(req));

        ListRoute<AttestationRun> findAllRoute = (req, res) ->
                attestationRunService.findAll();

        ListRoute<AttestationRun> findByEntityRefRoute =
                (req, res) -> attestationRunService.findByEntityReference(getEntityReference(req));

        ListRoute<AttestationRun> findByRecipientRoute = (req, res) ->
                attestationRunService.findByRecipient(getUsername(req));

        ListRoute<AttestationRun> findBySelectorRoute = ((request, response)
                -> attestationRunService.findByIdSelector(readIdSelectionOptionsFromBody(request)));

        ListRoute<AttestationRunResponseSummary> findResponseSummariesRoute = (req, res) ->
                attestationRunService
                        .findResponseSummaries();

        DatumRoute<AttestationCreateSummary> getCreateSummaryRoute = (req, res) ->
                attestationRunService
                        .getCreateSummary(readCreateCommand(req));

        DatumRoute<IdCommandResponse> attestationRunCreateRoute = (req, res) -> {
            if(!readCreateCommand(req).selectionOptions().entityReference().kind().equals(EntityKind.APPLICATION)) {
                ensureUserHasAttestationAdminRights(req);
            };

            return attestationRunService
                    .create(
                            getUsername(req),
                            readCreateCommand(req));
        };

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findAllPath, findAllRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findByRecipientPath, findByRecipientRoute);
        getForList(findResponseSummariesPath, findResponseSummariesRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForDatum(BASE_URL, attestationRunCreateRoute);
        postForDatum(getCreateSummaryPath, getCreateSummaryRoute);
    }


    // -- HELPERS ---

    private AttestationRunCreateCommand readCreateCommand(Request req) throws java.io.IOException {
        return readBody(req, AttestationRunCreateCommand.class);
    }


    private void ensureUserHasAttestationAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ATTESTATION_ADMIN);
    }
}
