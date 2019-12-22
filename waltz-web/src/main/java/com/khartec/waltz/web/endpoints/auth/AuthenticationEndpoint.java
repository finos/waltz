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

package com.khartec.waltz.web.endpoints.auth;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.model.settings.NamedSettings;
import com.khartec.waltz.model.user.AuthenticationResponse;
import com.khartec.waltz.model.user.ImmutableAuthenticationResponse;
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

import java.net.HttpURLConnection;
import java.net.URL;
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
                .flatMap(this::instantiateFilter)
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
            AuthenticationResponse authResponse = authenticate(login);

            if (authResponse.success()) {
                Algorithm algorithmHS = Algorithm.HMAC512(JWTUtilities.SECRET);

                String[] roles = userRoleService
                        .getUserRoles(authResponse.waltzUserName())
                        .toArray(new String[0]);

                String token = JWT.create()
                        .withIssuer(JWTUtilities.ISSUER)
                        .withSubject(authResponse.waltzUserName())
                        .withArrayClaim("roles", roles)
                        .withClaim("displayName", login.userName())
                        .withClaim("employeeId", login.userName())
                        .sign(algorithmHS);

                return newHashMap("token", token);
            } else {
                response.status(401);
                return authResponse.errorMessage();
            }
        }, transformer);

        before(mkPath("api", "*"), filter);

    }


    private AuthenticationResponse authenticate(LoginRequest loginRequest) {
        return settingsService
                .getValue(NamedSettings.externalAuthenticationEndpointUrl)
                .map(url -> doExternalAuth(loginRequest, url))
                .orElseGet(() -> doInternalAuth(loginRequest));
    }


    private AuthenticationResponse doExternalAuth(LoginRequest loginRequest, String url) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String loginParamsStr = mapper.writerFor(LoginRequest.class).writeValueAsString(loginRequest);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.getOutputStream().write(loginParamsStr.getBytes("UTF-8"));

            String responseStr = IOUtilities.readAsString(conn.getInputStream());
            return mapper.readValue(responseStr, AuthenticationResponse.class);
        } catch (Exception e) {
            return ImmutableAuthenticationResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }


    private AuthenticationResponse doInternalAuth(LoginRequest loginRequest) {

        ImmutableAuthenticationResponse.Builder builder = ImmutableAuthenticationResponse.builder()
                .waltzUserName(loginRequest.userName());

        if (userService.authenticate(loginRequest)) {
            return builder
                    .success(true)
                    .build();
        } else {
            return builder
                    .success(false)
                    .errorMessage("Invalid username/password")
                    .build();
        }
    }
}
