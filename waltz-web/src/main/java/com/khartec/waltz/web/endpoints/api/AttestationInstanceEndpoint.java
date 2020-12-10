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

import com.khartec.waltz.model.attestation.AttestEntityCommand;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.attestation.AttestationInstanceService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class AttestationInstanceEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AttestationInstanceEndpoint.class);
    private static final String BASE_URL = mkPath("api", "attestation-instance");

    private final AttestationInstanceService attestationInstanceService;
    private final UserRoleService userRoleService;


    @Autowired
    public AttestationInstanceEndpoint (AttestationInstanceService attestationInstanceService,
                                        UserRoleService userRoleService) {
        checkNotNull(attestationInstanceService, "attestationInstanceService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.attestationInstanceService = attestationInstanceService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String attestInstancePath = mkPath(BASE_URL, "attest", ":id");
        String attestEntityForUserPath = mkPath(BASE_URL, "attest-entity");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByRunIdPath = mkPath(BASE_URL, "run", ":id");
        String findUnattestedByUserPath = mkPath(BASE_URL, "unattested", "user");
        String findAllByUserPath = mkPath(BASE_URL, "all", "user");
        String findHistoricalForPendingByUserPath = mkPath(BASE_URL, "historical", "user");
        String findPersonsByInstancePath = mkPath(BASE_URL, ":id", "person");
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");

        DatumRoute<Boolean> attestInstanceRoute =
                (req, res) -> attestationInstanceService.attestInstance(
                            getId(req),
                            getUsername(req));

        ListRoute<AttestationInstance> findByEntityRefRoute =
                (req, res) -> attestationInstanceService.findByEntityReference(getEntityReference(req));

        ListRoute<AttestationInstance> findUnattestedByRecipientRoute =
                (req, res) -> attestationInstanceService.findByRecipient(getUsername(req), true);

        ListRoute<AttestationInstance> findAllByRecipientRoute =
                (req, res) -> attestationInstanceService.findByRecipient(getUsername(req), false);

        ListRoute<AttestationInstance> findHistoricalForPendingByRecipientRoute =
                (req, res) -> attestationInstanceService.findHistoricalForPendingByUserId(getUsername(req));

        ListRoute<AttestationInstance> findByRunIdRoute =
                (req, res) -> attestationInstanceService.findByRunId(getId(req));

        ListRoute<Person> findPersonsByInstanceRoute = (request, response) -> {
            long id = Long.valueOf(request.params("id"));
            return attestationInstanceService.findPersonsByInstanceId(id);
        };

        ListRoute<AttestationInstance> findBySelectorRoute = ((request, response)
                -> attestationInstanceService.findByIdSelector(readIdSelectionOptionsFromBody(request)));

        DatumRoute<Boolean> attestEntityForUserRoute =
                (req, res) -> attestationInstanceService.attestForEntity(getUsername(req), readCreateCommand(req));

        postForDatum(attestInstancePath, attestInstanceRoute);
        postForDatum(attestEntityForUserPath, attestEntityForUserRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findUnattestedByUserPath, findUnattestedByRecipientRoute);
        getForList(findAllByUserPath, findAllByRecipientRoute);
        getForList(findHistoricalForPendingByUserPath, findHistoricalForPendingByRecipientRoute);
        getForList(findByRunIdPath, findByRunIdRoute);
        getForList(findPersonsByInstancePath, findPersonsByInstanceRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
    }


    private int cleanupOrphansRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested orphan attestation cleanup", username);
        return attestationInstanceService.cleanupOrphans();
    }


    private AttestEntityCommand readCreateCommand(Request req) throws java.io.IOException {
        return readBody(req, AttestEntityCommand.class);
    }

}
