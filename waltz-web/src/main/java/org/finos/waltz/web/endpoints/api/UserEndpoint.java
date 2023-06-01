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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.service.user.UserService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.endpoints.auth.AuthenticationUtilities;
import org.finos.waltz.model.user.*;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.web.WebUtilities.requireAnyRole;


@Service
public class UserEndpoint implements Endpoint {


    private static final String BASE_URL = WebUtilities.mkPath("api", "user");

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

        String newUserPath = WebUtilities.mkPath(BASE_URL, "new-user");
        String updateRolesPath = WebUtilities.mkPath(BASE_URL, ":userName", "roles");
        String resetPasswordPath = WebUtilities.mkPath(BASE_URL, "reset-password");

        String deleteUserPath = WebUtilities.mkPath(BASE_URL, ":userName");

        String whoAmIPath = WebUtilities.mkPath(BASE_URL, "whoami");
        String findAllPath = WebUtilities.mkPath(BASE_URL);
        String getByUserIdPath = WebUtilities.mkPath(BASE_URL, "user-id", ":userId");

        String bulkUploadPreviewPath = WebUtilities.mkPath(BASE_URL, "bulk", ":mode", "preview");
        String bulkUploadPath = WebUtilities.mkPath(BASE_URL, "bulk", ":mode", "upload");


        // -- routes

        ListRoute<BulkUserOperationRowPreview> bulkUploadPreviewRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            String username = WebUtilities.getUsername(request);
            BulkUploadMode mode = BulkUploadMode.valueOf(request.params("mode"));
            String[] lines = request.body().split("\\R");
            return userRoleService.bulkUploadPreview(
                    mode,
                    asList(lines),
                    username);
        };

        DatumRoute<Integer> bulkUploadRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            String username = WebUtilities.getUsername(request);
            BulkUploadMode mode = BulkUploadMode.valueOf(request.params("mode"));
            String[] lines = request.body().split("\\R");
            return userRoleService.bulkUpload(
                    mode,
                    asList(lines),
                    username);
        };

        DatumRoute<Boolean> newUserRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            UserRegistrationRequest userRegRequest = WebUtilities.readBody(request, UserRegistrationRequest.class);
            return userService.registerNewUser(userRegRequest) == 1;
        };

        DatumRoute<Integer> updateRolesRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            String userName = WebUtilities.getUsername(request);
            String targetUserName = request.params("userName");
            UpdateRolesCommand cmd = WebUtilities.readBody(request, UpdateRolesCommand.class);
            return userRoleService.updateRoles(userName, targetUserName, cmd);
        };

        DatumRoute<Boolean> resetPasswordRoute = (request, response) -> {
            boolean validate = !userRoleService.hasAnyRole(WebUtilities.getUsername(request), SystemRole.USER_ADMIN, SystemRole.ADMIN);
            return userService.resetPassword(WebUtilities.readBody(request, PasswordResetRequest.class), validate);
        };

        DatumRoute<Boolean> deleteUserRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            String userName = request.params("userName");
            return userService.deleteUser(userName);
        };

        DatumRoute<User> whoAmIRoute = (request, response) -> {
            if (AuthenticationUtilities.isAnonymous(request)) {
                return UserUtilities.ANONYMOUS_USER;
            }

            String username = WebUtilities.getUsername(request);

            userService.ensureExists(username);

            return ImmutableUser.builder()
                    .userName(username)
                    .roles(userRoleService.getUserRoles(username))
                    .build();
        };

        ListRoute<User> findAllRoute = (request, response) -> userRoleService.findAllUsers();
        DatumRoute<User> getByUserIdRoute = (request, response) -> userRoleService.getByUserId(request.params("userId"));


        // --- register

        EndpointUtilities.postForList(bulkUploadPreviewPath, bulkUploadPreviewRoute);
        EndpointUtilities.postForDatum(bulkUploadPath, bulkUploadRoute);
        EndpointUtilities.postForDatum(newUserPath, newUserRoute);
        EndpointUtilities.postForDatum(updateRolesPath, updateRolesRoute);
        EndpointUtilities.postForDatum(resetPasswordPath, resetPasswordRoute);

        EndpointUtilities.deleteForDatum(deleteUserPath, deleteUserRoute);

        EndpointUtilities.getForDatum(whoAmIPath, whoAmIRoute);
        EndpointUtilities.getForDatum(getByUserIdPath, getByUserIdRoute);
        EndpointUtilities.getForList(findAllPath, findAllRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireAnyRole(
                userRoleService,
                request,
                SystemRole.USER_ADMIN,
                SystemRole.ADMIN);
    }

}
