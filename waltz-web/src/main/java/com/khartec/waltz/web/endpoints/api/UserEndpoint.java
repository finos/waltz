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
import com.khartec.waltz.model.user.User;
import com.khartec.waltz.model.user.UserRegistrationRequest;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.WebUtilities.requireRole;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class UserEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "user");

    private final UserService userService;
    private final UserRoleService userRoleService;


    @Autowired
    public UserEndpoint(UserService userService, UserRoleService userRoleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // -- paths

        String newUserPath = mkPath(BASE_URL, "new-user");
        String updateRolesPath = mkPath(BASE_URL, ":userName", "roles");
        String resetPasswordPath = mkPath(BASE_URL, ":userName", "reset-password");

        String deleteUserPath = mkPath(BASE_URL, ":userName");

        String whoAmIPath = mkPath(BASE_URL, "whoami");
        String findAllPath = mkPath(BASE_URL);
        String findUserPath = mkPath(BASE_URL, ":userName");


        // -- routes

        DatumRoute<Boolean> newUserRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.ADMIN);

            UserRegistrationRequest userRegRequest = readBody(request, UserRegistrationRequest.class);
            return userService.registerNewUser(userRegRequest) == 1;
        };
        DatumRoute<Boolean> updateRolesRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.ADMIN);

            String userName = request.params("userName");
            List<String> roles = (List<String>) readBody(request, List.class);
            return userRoleService.updateRoles(userName, map(roles, r -> Role.valueOf(r)));
        };
        DatumRoute<Boolean> resetPasswordRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.ADMIN);

            String userName = request.params("userName");
            String password = request.body().trim();
            return userService.resetPassword(userName, password);
        };

        DatumRoute<Boolean> deleteUserRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.ADMIN);

            String userName = request.params("userName");
            return userService.deleteUser(userName);
        };

        DatumRoute<Object> whoAmIRoute = (request, response) -> request.attribute("user");
        ListRoute<User> findAllRoute = (request, response) -> userRoleService.findAllUsers();
        DatumRoute<User> findUserRoute = (request, response) -> userRoleService.findByUserName(request.params("userName"));

        // --- register

        postForDatum(newUserPath, newUserRoute);
        postForDatum(updateRolesPath, updateRolesRoute);
        postForDatum(resetPasswordPath, resetPasswordRoute);

        deleteForDatum(deleteUserPath, deleteUserRoute);

        getForDatum(findUserPath, findUserRoute);
        getForDatum(whoAmIPath, whoAmIRoute);
        getForList(findAllPath, findAllRoute);
    }

}
