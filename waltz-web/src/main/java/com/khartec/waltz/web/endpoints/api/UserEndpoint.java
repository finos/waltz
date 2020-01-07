/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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

import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.map;
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
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);

            UserRegistrationRequest userRegRequest = readBody(request, UserRegistrationRequest.class);
            return userService.registerNewUser(userRegRequest) == 1;
        };

        DatumRoute<Boolean> updateRolesRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);

            String userName = getUsername(request);
            String targetUserName = request.params("userName");
            Set<String> roles = (Set<String>) readBody(request, Set.class);
            return userRoleService.updateRoles(userName, targetUserName, roles);
        };

        DatumRoute<Boolean> resetPasswordRoute = (request, response) -> {
            boolean validate = !userRoleService.hasAnyRole(getUsername(request), SystemRole.USER_ADMIN, SystemRole.ADMIN);
            return userService.resetPassword(readBody(request, PasswordResetRequest.class), validate);
        };

        DatumRoute<Boolean> deleteUserRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);

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
