/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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


import com.khartec.waltz.model.physical_flow_participant.ParticipationKind;
import com.khartec.waltz.model.physical_flow_participant.PhysicalFlowParticipant;
import com.khartec.waltz.service.physical_flow_participant.PhysicalFlowParticipantService;
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


    public PhysicalFlowParticipantEndpoint(PhysicalFlowParticipantService service) {
        checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {
        String findByPhysicalFlowIdPath = mkPath(BASE_URL, "physical-flow", ":id");
        String removePath = mkPath(BASE_URL, "physical-flow", ":physicalFlowId", ":kind", ":participantKind", ":participantId");
        String addPath = mkPath(BASE_URL, "physical-flow", ":physicalFlowId", ":kind", ":participantKind", ":participantId");

        ListRoute<PhysicalFlowParticipant> findByPhysicalFlowIdRoute =
                (request, response) -> service.findByPhysicalFlowId(getId(request));

        DatumRoute<Boolean> removeRoute = (request, response) -> {
            // TODO: check perms
            return service.remove(
                    getLong(request, "physicalFlowId"),
                    readEnum(request, "kind", ParticipationKind.class, (s) -> null),
                    getEntityReference(request, "participantKind", "participantId"),
                    getUsername(request));
        };

        DatumRoute<Boolean> addRoute = (request, response) -> {
            // TODO: check perms
            return service.add(
                    getLong(request, "physicalFlowId"),
                    readEnum(request, "kind", ParticipationKind.class, (s) -> null),
                    getEntityReference(request, "participantKind", "participantId"),
                    getUsername(request));
        };

        getForList(findByPhysicalFlowIdPath, findByPhysicalFlowIdRoute);
        deleteForDatum(removePath, removeRoute);
        postForDatum(addPath, addRoute);
    }
}
