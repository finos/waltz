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

import com.khartec.waltz.model.accesslog.AccessLog;
import com.khartec.waltz.model.accesslog.ImmutableAccessLog;
import com.khartec.waltz.service.access_log.AccessLogService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;


@Service
public class AccessLogEndpoint implements Endpoint {


    private static final Logger LOG = LoggerFactory.getLogger(AccessLogEndpoint.class);
    private static final String BASE_URL = mkPath("api", "access-log");

    private final AccessLogService accessLogService;


    @Autowired
    public AccessLogEndpoint(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }


    @Override
    public void register() {

        EndpointUtilities.getForList(mkPath(BASE_URL, "user", ":userId"),
                (request, response) -> accessLogService.findForUserId(request.params("userId")));


        EndpointUtilities.post(mkPath(BASE_URL, ":state", ":params"), (request, response) -> {
            AccessLog accessLog = ImmutableAccessLog.builder()
                    .userId(WebUtilities.getUser(request).userName())
                    .state(request.params("state"))
                    .params(request.params("params"))
                    .build();

            return accessLogService.write(accessLog) == 1;
        });
    }
}
