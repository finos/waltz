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

package org.finos.waltz.web.endpoints.auth;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.model.settings.NamedSettings;
import org.finos.waltz.model.user.AuthenticationResponse;
import org.finos.waltz.model.user.ImmutableAuthenticationResponse;
import org.finos.waltz.model.user.LoginRequest;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.service.user.UserService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import spark.Filter;
import spark.Spark;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Supplier;

import static org.finos.waltz.common.MapUtilities.newHashMap;


@Service
@Configuration
@PropertySource(value = "classpath:waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.waltz/waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true)
@ComponentScan(value={"org.finos.waltz.data"})
public class AuthenticationEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("authentication");

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class);

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final SettingsService settingsService;
    private final Filter filter;


    @Value("${oauth.token_url}")
    private String TOKEN_URL;
    @Value("${oauth.userinfo_url}")
    private String USERINFO_URL;
    @Value("${oauth.code_verifier}")
    private String CODE_VERIFIER;
    @Value("${oauth.redirect_uri}")
    private String REDIRECT_URI;

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

        Spark.post(WebUtilities.mkPath(BASE_URL, "login"), (request, response) -> {

            LoginRequest login = WebUtilities.readBody(request, LoginRequest.class);
            AuthenticationResponse authResponse = authenticate(login);

            if (authResponse.success()) {
                Algorithm algorithmHS = Algorithm.HMAC512(JWTUtilities.SECRET);

                String[] roles = userRoleService
                        .getUserRoles(authResponse.waltzUserName())
                        .toArray(new String[0]);

                // LOG.info("login via waltz for:" login.userName());
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
        }, WebUtilities.transformer);

        Spark.post(WebUtilities.mkPath(BASE_URL, "oauth"), (request, response) -> {
            // parse code response after successful authorization
            Algorithm algorithmHS = Algorithm.HMAC512(JWTUtilities.SECRET);
            String[] vals =  parseCodeResponse(request.body());
            String OAuthCode = vals[0];
            String clientId = vals[1];

            String accessToken = getAccessToken(OAuthCode, clientId);
            JSONObject json = fetchOAuthUserInfo(accessToken);

            // parse user info from json object
            String email = json.get("email").toString().toLowerCase();
            String subname = json.get("subname").toString();
            String name = json.get("name").toString();

            String[] roles = userRoleService
                    .getUserRoles(email)
                    .toArray(new String[0]);

            LOG.info("login via sso for: email:" + email);

            String token = JWT.create()
                    .withIssuer(JWTUtilities.ISSUER)
                    .withSubject("email")
                    .withArrayClaim("roles", roles)
                    .withClaim("displayName", name)
                    .withClaim("employeeId", subname)
                    .sign(algorithmHS);

            return newHashMap("token", token);
        }, WebUtilities.transformer);

        Spark.before(WebUtilities.mkPath("api", "*"), filter);

    }

    private String[] parseCodeResponse(String RequestBody) throws ParseException {
        // json stringify
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(RequestBody);
        return new String[]{(String) json.get("code"), (String) json.get("clientId")};

    }

    private JSONObject fetchOAuthUserInfo(String accessToken) throws IOException, ParseException {
        URL userinfoURL = new URL(USERINFO_URL);
        HttpURLConnection emailConnection = (HttpURLConnection) userinfoURL.openConnection();
        emailConnection.setRequestMethod("POST");
        emailConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
        return parseUserInfo(emailConnection);
    }

    private String getAccessToken(String OAuthCode, String clientId) {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(paramBuilder(OAuthCode, clientId).getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
                LOG.info("IOException on conn.getOutputStream(): " + e.getMessage());
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseAccessToken(conn);
            } else {
                LOG.error("HttpURLConnection: Error getting access token from OAuth provider, CODE: " + responseCode);
            }

        } catch (IOException | ParseException e) {
            LOG.error("Error getting access token from OAuth provider", e);
        }
        return null;
    }

    private String parseAccessToken(HttpURLConnection conn) throws IOException, ParseException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());
            return json.get("access_token").toString();

        }
    }

    private JSONObject parseUserInfo(HttpURLConnection conn) throws IOException, ParseException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());
            return json;

        }
    }

    private String paramBuilder(String OAuthCode, String clientId){
        return  "&grant_type=authorization_code" +
                "&code=" + OAuthCode +
                "&redirect_uri=" + REDIRECT_URI +
                "&client_id=" + clientId +
                "&client_verifier=" + CODE_VERIFIER;
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
