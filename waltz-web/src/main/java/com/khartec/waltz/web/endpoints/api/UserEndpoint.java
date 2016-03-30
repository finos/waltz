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

import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.model.user.UserRegistrationRequest;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Spark;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.WebUtilities.requireRole;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.post;
import static spark.Spark.delete;
import static spark.Spark.get;


@Service
public class UserEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "user");

    private final UserService userService;


    @Autowired
    public UserEndpoint(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void register() {
        getForDatum(mkPath(BASE_URL, "whoami"), ((request, response) -> request.attribute("user")));
        getForList(mkPath(BASE_URL), ((request, response) -> userService.findAllUsers()));
        getForDatum(mkPath(BASE_URL, ":userName"), (request, response) -> userService.findByUserName(request.params("userName")));

        post(mkPath(BASE_URL, "new-user"), (request, response) -> {
            requireRole(userService, request, Role.ADMIN);

            UserRegistrationRequest userRegRequest = readBody(request, UserRegistrationRequest.class);
            return userService.registerNewUser(userRegRequest) == 1;
        });

        post(mkPath(BASE_URL, ":userName", "roles"), (request, response) -> {
            requireRole(userService, request, Role.ADMIN);

            String userName = request.params("userName");
            List<String> roles = (List<String>) readBody(request, List.class);
            return userService.updateRoles(userName, map(roles, r -> Role.valueOf(r)));
        });

        post(mkPath(BASE_URL, ":userName", "reset-password"), (request, response) -> {
            requireRole(userService, request, Role.ADMIN);

            String userName = request.params("userName");
            String password = request.body().trim();
            return userService.resetPassword(userName, password);
        });

        delete(mkPath(BASE_URL, ":userName"), (request, response) -> {
            requireRole(userService, request, Role.ADMIN);

            String userName = request.params("userName");
            return userService.deleteUser(userName);
        });
    }

}
