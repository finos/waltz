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
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.user.UserRoleService;
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
        getForList(
                mkPath(BASE_URL),
                (request, response) -> service.findAll());

        getForList(
                mkPath(BASE_URL, "search", ":query"),
                (request, response) -> service.search(request.params("query")));

        getForDatum(mkPath(BASE_URL, ":id"),
                (request, response) -> service.getById(getId(request)));


        getForDatum(mkPath(BASE_URL, ":id", "hierarchy"),
                (request, response) -> service.getHierarchyById(getId(request)));


        post(mkPath(BASE_URL, ":id", "description"),
                (request, response) -> {

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

                });

        ListRoute<OrganisationalUnit> findByIdsRoute = (req, res) -> service.findByIds(readBody(req, Long[].class));
        postForList(mkPath(BASE_URL, "by-ids"), findByIdsRoute);
    }

}
