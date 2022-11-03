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

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.attestation.*;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.attestation.AttestationInstanceService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;


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
        String findLatestMeasurableAttestationsPath = mkPath(BASE_URL, "latest", "measurable-category", "entity", ":kind", ":id");
        String findApplicationAttestationInstancesForKindAndSelectorPath = mkPath(BASE_URL, "applications", "attested-entity", ":kind", ":id");
        String findApplicationAttestationSummaryForSelectorPath = mkPath(BASE_URL, "app-summary");
        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");
        String reassignRecipientsPath = mkPath(BASE_URL, "reassign-recipients");
        String getCountsOfRecipientsToReassignPath = mkPath(BASE_URL, "reassign-counts");

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
            long id = Long.parseLong(request.params("id"));
            return attestationInstanceService.findPersonsByInstanceId(id);
        };

        ListRoute<AttestationInstance> findBySelectorRoute = ((request, response)
                -> attestationInstanceService.findByIdSelector(readIdSelectionOptionsFromBody(request)));

        ListRoute<LatestMeasurableAttestationInfo> findLatestMeasurableAttestationsRoute = ((request, response)
                -> attestationInstanceService.findLatestMeasurableAttestations(getEntityReference(request)));

        ListRoute<ApplicationAttestationInstanceSummary> findApplicationAttestationInstancesForKindAndSelectorRoute = ((request, response) -> {
            EntityKind attestedKind = getKind(request);
            Long attestedId = StringUtilities.parseLong(request.params("id"), null);
            ApplicationAttestationInstanceInfo applicationAttestationInstanceInfo = readBody(request, ApplicationAttestationInstanceInfo.class);

            return attestationInstanceService.findApplicationAttestationInstancesForKindAndSelector(
                    attestedKind,
                    attestedId,
                    applicationAttestationInstanceInfo);
        });

        ListRoute<ApplicationAttestationSummaryCounts> findApplicationAttestationSummaryForSelectorRoute = ((request, response)
                -> attestationInstanceService.findAttestationInstanceSummaryForSelector(readBody(request, ApplicationAttestationInstanceInfo.class)));

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
        getForList(findLatestMeasurableAttestationsPath, findLatestMeasurableAttestationsRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForList(findApplicationAttestationInstancesForKindAndSelectorPath, findApplicationAttestationInstancesForKindAndSelectorRoute);
        postForList(findApplicationAttestationSummaryForSelectorPath, findApplicationAttestationSummaryForSelectorRoute);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
        postForDatum(reassignRecipientsPath, this::reassignRecipientsRoute);
        getForDatum(getCountsOfRecipientsToReassignPath, this::getCountsOfRecipientsToReassign);
    }


    private int cleanupOrphansRoute(Request request, Response response) {
        requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested orphan attestation cleanup", username);
        return attestationInstanceService.cleanupOrphans();
    }


    private SyncRecipientsResponse reassignRecipientsRoute(Request request, Response response) {
        requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested reassign recipients for attestations", username);
        return attestationInstanceService.reassignRecipients();
    }


    private SyncRecipientsResponse getCountsOfRecipientsToReassign(Request request, Response response) {
        return attestationInstanceService.getCountsOfRecipientsToReassign();
    }


    private AttestEntityCommand readCreateCommand(Request req) throws java.io.IOException {
        return readBody(req, AttestEntityCommand.class);
    }

}
