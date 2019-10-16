/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.external_identifier.ExternalIdentifierService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class ExternalIdentifierEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "external-identifier");
    private final ExternalIdentifierService externalIdentifierService;
    private final UserRoleService userRoleService;


    @Autowired
    public ExternalIdentifierEndpoint(ExternalIdentifierService externalIdentifierService,
                                      UserRoleService userRoleService) {
        checkNotNull(externalIdentifierService, "externalIdentifierService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.externalIdentifierService = externalIdentifierService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        ListRoute<ExternalIdentifier> findForEntityReference = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            return externalIdentifierService.findByEntityReference(ref);
        };


        DatumRoute<Integer> deleteRoute = (req, resp) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            EntityReference ref = getEntityReference(req);
            String system = req.params("system");
            String externalId = req.splat()[0];

            return externalIdentifierService.delete(ref, externalId, system, getUsername(req));
        };

        DatumRoute<Integer> createRoute = (req, resp) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            EntityReference ref = getEntityReference(req);
            String externalId = req.splat()[0];

            return externalIdentifierService.create(ref, externalId, getUsername(req));
        };


        // delete
        deleteForDatum(mkPath(BASE_URL, "entity", ":kind", ":id", ":system", "externalId", "*"), deleteRoute);

        postForDatum(mkPath(BASE_URL, "entity", ":kind", ":id", "externalId", "*"), createRoute);

        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), findForEntityReference);
    }

}
