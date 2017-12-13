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

import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.user.Role;
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
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


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
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByRunIdPath = mkPath(BASE_URL, "run", ":id");
        String findUnattestedByUserPath = mkPath(BASE_URL, "unattested", "user");
        String findAllByUserPath = mkPath(BASE_URL, "all", "user");
        String findHistoricalForPendingByUserPath = mkPath(BASE_URL, "historical", "user");
        String findPersonsByInstancePath = mkPath(BASE_URL, ":id", "person");
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


        postForDatum(attestInstancePath, attestInstanceRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findUnattestedByUserPath, findUnattestedByRecipientRoute);
        getForList(findAllByUserPath, findAllByRecipientRoute);
        getForList(findHistoricalForPendingByUserPath, findHistoricalForPendingByRecipientRoute);
        getForList(findByRunIdPath, findByRunIdRoute);
        getForList(findPersonsByInstancePath, findPersonsByInstanceRoute);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
    }


    private int cleanupOrphansRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested orphan attestation cleanup", username);
        return attestationInstanceService.cleanupOrphans();
    }

}
