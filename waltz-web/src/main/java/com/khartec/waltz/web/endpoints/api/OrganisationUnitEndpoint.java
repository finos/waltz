/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnitHierarchy;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.action.FieldChange;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class OrganisationUnitEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "org-unit");
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationUnitEndpoint.class);

    private final OrganisationalUnitService service;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public OrganisationUnitEndpoint(OrganisationalUnitService service,
                                    ChangeLogService changeLogService,
                                    UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.service = service;
        this.changeLogService = changeLogService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findAllPath = mkPath(BASE_URL);
        String searchPath = mkPath(BASE_URL, "search", ":query");
        String findByIdsPath = mkPath(BASE_URL, "by-ids");
        String getByIdPath = mkPath(BASE_URL, ":id");
        String getHierarchyPath = mkPath(BASE_URL, ":id", "hierarchy");
        String postDescriptionPath = mkPath(BASE_URL, ":id", "description");


        ListRoute<OrganisationalUnit> findAllRoute = (request, response) -> service.findAll();
        ListRoute<OrganisationalUnit> searchRoute = (request, response) -> service.search(request.params("query"));
        ListRoute<OrganisationalUnit> findByIdsRoute = (req, res) -> service.findByIds(readBody(req, Long[].class));

        DatumRoute<OrganisationalUnit> getByIdRoute = (request, response) -> service.getById(getId(request));
        DatumRoute<OrganisationalUnitHierarchy> getHierarchyRoute = (request, response) -> service.getHierarchyById(getId(request));

        DatumRoute<Integer> postDescriptionRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.ORGUNIT_EDITOR);

            ChangeLog changeLogEntry = ImmutableChangeLog.builder()
                    .parentReference(ImmutableEntityReference.builder()
                            .id(getId(request))
                            .kind(EntityKind.ORG_UNIT)
                            .build())
                    .message("Description updated")
                    .userId(getUsername(request))
                    .severity(Severity.INFORMATION)
                    .build();

            response.type(WebUtilities.TYPE_JSON);
            FieldChange valueChange = readBody(request, FieldChange.class);
            int updateCount = service.updateDescription(
                    getId(request),
                    valueChange.current().orElse(null));

            changeLogService.write(changeLogEntry);

            return updateCount;
        };


        getForList(findAllPath, findAllRoute);
        getForList(searchPath, searchRoute);
        postForList(findByIdsPath, findByIdsRoute);

        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getHierarchyPath, getHierarchyRoute);
        postForDatum(postDescriptionPath, postDescriptionRoute);

    }

}
