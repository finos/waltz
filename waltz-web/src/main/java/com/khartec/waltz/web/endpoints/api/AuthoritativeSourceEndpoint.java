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


import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static spark.Spark.delete;


@Service
public class AuthoritativeSourceEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "authoritative-source");

    private final AuthoritativeSourceService authoritativeSourceService;
    private final ChangeLogService changeLogService;


    @Autowired
    public AuthoritativeSourceEndpoint(
            AuthoritativeSourceService authoritativeSourceService,
            ChangeLogService changeLogService) {
        checkNotNull(authoritativeSourceService, "authoritativeSourceService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");

        this.authoritativeSourceService = authoritativeSourceService;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {
        EndpointUtilities.getForList(WebUtilities.mkPath(BASE_URL, "kind", ":kind"), (request, response)
                -> authoritativeSourceService.findByEntityKind(WebUtilities.getKind(request)));

        EndpointUtilities.getForList(WebUtilities.mkPath(BASE_URL, "kind", ":kind", ":id"), (request, response)
                -> authoritativeSourceService.findByEntityReference(WebUtilities.getEntityReference(request)));

        EndpointUtilities.getForList(WebUtilities.mkPath(BASE_URL, "app", ":id"), (request, response)
                -> authoritativeSourceService.findByApplicationId(WebUtilities.getId(request)));

        EndpointUtilities.post(WebUtilities.mkPath(BASE_URL, "id", ":id"), (request, response) -> {
            String ratingStr = request.body();
            Rating rating = Rating.valueOf(ratingStr);
            authoritativeSourceService.update(WebUtilities.getId(request), rating);
            return "done";
        });

        delete(WebUtilities.mkPath(BASE_URL, "id", ":id"), (request, response) -> {
            long id = WebUtilities.getId(request);
            AuthoritativeSource authSource = authoritativeSourceService.getById(id);
            if (authSource == null) {
                return "done";
            }

            String msg = String.format(
                    "Removed %s as an %s authoritative source for %s",
                    authSource.applicationReference().name().orElse("an application"),
                    authSource.rating().name(),
                    authSource.dataType());

            ChangeLog log = ImmutableChangeLog.builder()
                    .message(msg)
                    .severity(Severity.INFORMATION)
                    .userId(WebUtilities.getUser(request).userName())
                    .parentReference(authSource.parentReference())
                    .build();

            changeLogService.write(log);
            authoritativeSourceService.remove(id);

            return "done";
        });

        EndpointUtilities.post(WebUtilities.mkPath(BASE_URL, "kind", ":kind", ":id", ":dataType", ":appId"), (request, response) -> {
            EntityReference parentRef = WebUtilities.getEntityReference(request);
            String dataType = request.params("dataType");
            Long appId = WebUtilities.getLong(request, "appId");

            String ratingStr = request.body();
            Rating rating = Rating.valueOf(ratingStr);

            authoritativeSourceService.insert(parentRef, dataType, appId, rating);
            return "done";
        });
    }

}
