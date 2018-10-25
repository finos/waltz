/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import com.khartec.waltz.model.user.*;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.auth.AuthenticationUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.web.WebUtilities.*;
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
        String resetPasswordPath = mkPath(BASE_URL, "reset-password");

        String deleteUserPath = mkPath(BASE_URL, ":userName");

        String whoAmIPath = mkPath(BASE_URL, "whoami");
        String findAllPath = mkPath(BASE_URL);
        String getByUserIdPath = mkPath(BASE_URL, "user-id", ":userId");


        // -- routes

        DatumRoute<Boolean> newUserRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, Role.USER_ADMIN, Role.ADMIN);

            UserRegistrationRequest userRegRequest = readBody(request, UserRegistrationRequest.class);
            return userService.registerNewUser(userRegRequest) == 1;
        };

        DatumRoute<Boolean> updateRolesRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, Role.USER_ADMIN, Role.ADMIN);

            String userName = getUsername(request);
            String targetUserName = request.params("userName");
            List<String> roles = (List<String>) readBody(request, List.class);
            return userRoleService.updateRoles(userName, targetUserName, map(roles, Role::valueOf));
        };

        DatumRoute<Boolean> resetPasswordRoute = (request, response) -> {
            boolean validate = !userRoleService.hasAnyRole(getUsername(request), Role.USER_ADMIN, Role.ADMIN);
            return userService.resetPassword(readBody(request, PasswordResetRequest.class), validate);
        };

        DatumRoute<Boolean> deleteUserRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, Role.USER_ADMIN, Role.ADMIN);

            String userName = request.params("userName");
            return userService.deleteUser(userName);
        };

        DatumRoute<User> whoAmIRoute = (request, response) -> {
            if (AuthenticationUtilities.isAnonymous(request)) {
                return UserUtilities.ANONYMOUS_USER;
            }

            String username = getUsername(request);

            userService.ensureExists(username);

            return ImmutableUser.builder()
                    .userName(username)
                    .roles(userRoleService.getUserRoles(username))
                    .build();
        };

        ListRoute<User> findAllRoute = (request, response) -> userRoleService.findAllUsers();
        DatumRoute<User> getByUserIdRoute = (request, response) -> userRoleService.getByUserId(request.params("userId"));


        // --- register

        postForDatum(newUserPath, newUserRoute);
        postForDatum(updateRolesPath, updateRolesRoute);
        postForDatum(resetPasswordPath, resetPasswordRoute);

        deleteForDatum(deleteUserPath, deleteUserRoute);

        getForDatum(whoAmIPath, whoAmIRoute);
        getForDatum(getByUserIdPath, getByUserIdRoute);
        getForList(findAllPath, findAllRoute);
    }

}
