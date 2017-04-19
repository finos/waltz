/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.attestation.AttestationService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class AttestationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "attestation");

    private final AttestationService attestationService;
    private final UserRoleService userRoleService;


    @Autowired
    public AttestationEndpoint(AttestationService attestationService,
                               UserRoleService userRoleService) {
        checkNotNull(attestationService, "attestationService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.attestationService = attestationService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String calculateForFlowDiagramsPath = mkPath(BASE_URL, "calculate-all", "flow-diagram");
        String calculateForLogicalDecorators = mkPath(BASE_URL, "calculate-all", "logical-flow-decorator");


        getForList(
                findForEntityPath,
                (request, response) -> attestationService.findForEntity(getEntityReference(request)));

        getForDatum(calculateForFlowDiagramsPath, this::calculateForFlowDiagramsRoute);
        getForDatum(calculateForLogicalDecorators, this::calculateForLogicalFlowDecoratorsRoute);
    }


    private boolean calculateForFlowDiagramsRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.ADMIN);
        return attestationService.recalculateForFlowDiagrams();
    }


    private boolean calculateForLogicalFlowDecoratorsRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.ADMIN);
        return attestationService.recalculateForLogicalFlowDecorators();
    }

}
