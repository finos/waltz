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


import com.khartec.waltz.model.IdCommandResponse;
import com.khartec.waltz.model.attestation.AttestationCreateSummary;
import com.khartec.waltz.model.attestation.AttestationRun;
import com.khartec.waltz.model.attestation.AttestationRunCreateCommand;
import com.khartec.waltz.model.attestation.AttestationRunResponseSummary;
import com.khartec.waltz.model.user.Role;
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

        ListRoute<AttestationRunResponseSummary> findResponseSummariesRoute = (req, res) ->
                attestationRunService
                        .findResponseSummaries();

        DatumRoute<AttestationCreateSummary> getCreateSummaryRoute = (req, res) ->
                attestationRunService
                        .getCreateSummary(readCreateCommand(req));

        DatumRoute<IdCommandResponse> attestationRunCreateRoute = (req, res) -> {
            ensureUserHasAttestationAdminRights(req);
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
        postForDatum(BASE_URL, attestationRunCreateRoute);
        postForDatum(getCreateSummaryPath, getCreateSummaryRoute);
    }


    // -- HELPERS ---

    private AttestationRunCreateCommand readCreateCommand(Request req) throws java.io.IOException {
        return readBody(req, AttestationRunCreateCommand.class);
    }


    private void ensureUserHasAttestationAdminRights(Request request) {
        requireRole(userRoleService, request, Role.ATTESTATION_ADMIN);
    }
}
