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


import com.khartec.waltz.model.physical_flow_participant.ParticipationKind;
import com.khartec.waltz.model.physical_flow_participant.PhysicalFlowParticipant;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.physical_flow_participant.PhysicalFlowParticipantService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class PhysicalFlowParticipantEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowParticipantEndpoint.class);
    private static final String BASE_URL = mkPath("api", "physical-flow-participant");

    private final PhysicalFlowParticipantService service;
    private final UserRoleService userRoleService;

    public PhysicalFlowParticipantEndpoint(PhysicalFlowParticipantService participantService,
                                           UserRoleService userRoleService) {
        checkNotNull(participantService, "participantService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.service = participantService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByPhysicalFlowIdPath = mkPath(BASE_URL, "physical-flow", ":id");
        String findByParticipantPath = mkPath(BASE_URL, "participant", ":kind", ":id");
        String removePath = mkPath(BASE_URL, "physical-flow", ":physicalFlowId", ":kind", ":participantKind", ":participantId");
        String addPath = mkPath(BASE_URL, "physical-flow", ":physicalFlowId", ":kind", ":participantKind", ":participantId");

        ListRoute<PhysicalFlowParticipant> findByPhysicalFlowIdRoute =
                (request, response) -> service.findByPhysicalFlowId(getId(request));

        ListRoute<PhysicalFlowParticipant> findByParticipantRoute =
                (request, response) -> service.findByParticipant(getEntityReference(request));

        DatumRoute<Boolean> removeRoute = (request, response) -> {
            requireRole(
                    userRoleService,
                    request,
                    SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            return service.remove(
                    getLong(request, "physicalFlowId"),
                    readEnum(request, "kind", ParticipationKind.class, (s) -> null),
                    getEntityReference(request, "participantKind", "participantId"),
                    getUsername(request));
        };

        DatumRoute<Boolean> addRoute = (request, response) -> {
            requireRole(
                    userRoleService,
                    request,
                    SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            return service.add(
                    getLong(request, "physicalFlowId"),
                    readEnum(request, "kind", ParticipationKind.class, (s) -> null),
                    getEntityReference(request, "participantKind", "participantId"),
                    getUsername(request));
        };

        getForList(findByPhysicalFlowIdPath, findByPhysicalFlowIdRoute);
        getForList(findByParticipantPath, findByParticipantRoute);
        deleteForDatum(removePath, removeRoute);
        postForDatum(addPath, addRoute);
    }
}
