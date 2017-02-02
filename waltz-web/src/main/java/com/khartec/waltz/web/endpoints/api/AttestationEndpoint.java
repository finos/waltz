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


import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.Attestation;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.attestation.AttestationService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class AttestationEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AttestationEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "attestation");

    private final AttestationService attestationService;
    private final UserRoleService userRoleService;


    @Autowired
    public AttestationEndpoint(AttestationService attestationService, UserRoleService userRoleService) {
        checkNotNull(attestationService, "attestationService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.attestationService = attestationService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // read
        getForList(mkPath(BASE_URL, ":kind", ":id"), this::findForEntityRoute);

        // create
        postForDatum(mkPath(BASE_URL, "update"), this::createRoute);
    }


    private List<Attestation> findForEntityRoute(Request request, Response response) {
        EntityReference entityReference = getEntityReference(request);
        return attestationService.findForEntity(entityReference);
    }


    private boolean createRoute(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);

        Attestation attestation = readBody(request, Attestation.class);
        return attestationService.create(attestation, getUsername(request));
    }


    private void ensureUserHasEditRights(Request request) {
        List<Role> editRoles = Role.allNames()
                .stream()
                .filter(r -> r.contains("EDITOR") || r.contains("ADMIN"))
                .map(s -> Enum.valueOf(Role.class, s))
                .collect(Collectors.toList());

        requireRole(userRoleService,
                request,
                (Role[]) editRoles.toArray());
    }

}
