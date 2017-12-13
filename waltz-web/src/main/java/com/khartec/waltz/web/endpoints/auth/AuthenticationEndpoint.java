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

package com.khartec.waltz.web.endpoints.auth;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.khartec.waltz.model.settings.NamedSettings;
import com.khartec.waltz.model.user.LoginRequest;
import com.khartec.waltz.service.settings.SettingsService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Filter;

import java.util.Optional;
import java.util.function.Supplier;

import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.before;
import static spark.Spark.post;


@Service
public class AuthenticationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("auth");

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class);

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final SettingsService settingsService;
    private final Filter filter;


    @Autowired
    public AuthenticationEndpoint(UserService userService,
                                  UserRoleService userRoleService,
                                  SettingsService settingsService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.settingsService = settingsService;

        this.filter = settingsService
                .getValue(NamedSettings.authenticationFilter)
                .flatMap(className -> instantiateFilter(className))
                .orElseGet(createDefaultFilter());
    }


    private Supplier<Filter> createDefaultFilter() {
        return () -> {
            LOG.info("Using default (jwt) authentication filter");
            return new JWTAuthenticationFilter(settingsService);
        };
    }


    private Optional<Filter> instantiateFilter(String className) {
        try {
            LOG.info("Setting authentication filter to: " + className);

            Filter filter = (Filter) Class
                    .forName(className)
                    .getConstructor(SettingsService.class)
                    .newInstance(settingsService);

            return Optional.of(filter);
        } catch (Exception e) {
            LOG.error("Cannot instantiate authentication filter class: " + className, e);
            return Optional.empty();
        }
    }


    @Override
    public void register() {

        post(mkPath(BASE_URL, "login"), (request, response) -> {

            LoginRequest login = readBody(request, LoginRequest.class);
            if (userService.authenticate(login)) {
                Algorithm algorithmHS = Algorithm.HMAC512(JWTUtilities.SECRET);

                String[] roles = userRoleService
                        .getUserRoles(login.userName()).stream()
                        .map(r -> r.name())
                        .toArray(size -> new String[size]);

                String token = JWT.create()
                        .withIssuer(JWTUtilities.ISSUER)
                        .withSubject(login.userName())
                        .withArrayClaim("roles", roles)
                        .withClaim("displayName", login.userName())
                        .withClaim("employeeId", login.userName())
                        .sign(algorithmHS);

                return newHashMap("token", token);
            } else {
                response.status(401);
                return "Unknown user/password";
            }
        }, transformer);

        before(mkPath("api", "*"), filter);

    }

}
