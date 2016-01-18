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

    import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.action.FieldChange;
import com.khartec.waltz.web.endpoints.Endpoint;
    import com.khartec.waltz.web.WebUtilities;
    import com.khartec.waltz.web.endpoints.EndpointUtilities;
    import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


    @Service
public class OrganisationUnitEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "org-unit");
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationUnitEndpoint.class);

    private final OrganisationalUnitService service;
    private final ChangeLogService changeLogService;
    private final UserService userService;


    @Autowired
    public OrganisationUnitEndpoint(OrganisationalUnitService service,
                                    ChangeLogService changeLogService,
                                    UserService userService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userService, "userService must not be null");


        this.service = service;
        this.changeLogService = changeLogService;
        this.userService = userService;
    }


    @Override
    public void register() {
        EndpointUtilities.getForList(
                WebUtilities.mkPath(BASE_URL),
                (request, response) -> service.findAll());

        EndpointUtilities.getForList(
                WebUtilities.mkPath(BASE_URL, "search", ":query"),
                (request, response) -> service.search(request.params("query")));

        EndpointUtilities.getForDatum(WebUtilities.mkPath(BASE_URL, ":id"),
                (request, response) -> service.getById(WebUtilities.getId(request)));

        EndpointUtilities.post(WebUtilities.mkPath(BASE_URL, ":id", "description"),
                (request, response) -> {

                    WebUtilities.requireRole(userService, request, Role.ORGUNIT_EDITOR);

                    ChangeLog changeLogEntry = ImmutableChangeLog.builder()
                            .parentReference(ImmutableEntityReference.builder()
                                    .id(WebUtilities.getId(request))
                                    .kind(EntityKind.ORG_UNIT)
                                    .build())
                            .message("Description updated")
                            .userId(WebUtilities.getUser(request).userName())
                            .severity(Severity.INFORMATION)
                            .build();

                    response.type(WebUtilities.TYPE_JSON);
                    FieldChange valueChange = WebUtilities.readBody(request, FieldChange.class);
                    int updateCount = service.updateDescription(
                            WebUtilities.getId(request),
                            valueChange.current().orElse(null));

                    changeLogService.write(changeLogEntry);

                    return updateCount;

                });

        EndpointUtilities.post(WebUtilities.mkPath(BASE_URL, "by-ids"),
                (req, res) -> {
                    List<Long> ids = (List<Long>) WebUtilities.readBody(req, List.class);
                    if (ListUtilities.isEmpty(ids)) {
                        return Collections.emptyList();
                    }
                    return service.findByIds(ids);
                });

    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BookmarksEndpoint{");
        sb.append('}');
        return sb.toString();
    }
}
