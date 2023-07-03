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

package org.finos.waltz.service.oauth;


//import org.finos.waltz.model.user.UserRegistrationRequest;
import org.finos.waltz.service.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

// TODO: This class is not used anywhere in the codebase.  It is not referenced by any other class, find another method (actualyl using framework)
import spark.Request;

import org.finos.waltz.service.user.UserService;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
@Configuration
@PropertySource(value = "classpath:waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.waltz/waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true)
@ComponentScan(value={"org.finos.waltz.data"})
public class OAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthService.class);

    @Value("${oauth.client_id}")
    private String CLIENT_ID;
    @Value("${oauth.client_secret}")
    private String CLIENT_SECRET;
    @Value("${oauth.code_url}")
    private String CODE_URL;
    @Value("${oauth.token_url}")
    private String TOKEN_URL;
    @Value("${oauth.email_url}")
    private String EMAIL_URL;


    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_HEADER_ACCEPT = "application/x-www-form-urlencoded";
    private static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_TYPE_BEARER = "Bearer ";
    private final UserService UserService;

    @Autowired
    public OAuthService(UserService UserService) {

        checkNotNull(UserService, "UserService cannot be null");
        this.UserService = UserService;
    }
    public String getAccessToken(String OAuthCode) {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(HTTP_METHOD_POST);
            conn.setRequestProperty("Accept", HTTP_HEADER_ACCEPT);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(getGitPostParams(OAuthCode).getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parse_access_token(conn);
            } else {
                handleError(responseCode, conn);
            }

        } catch (IOException e) {
            // Log the exception properly, don't just print the stack trace
            e.printStackTrace();
        }
        return null;
    }

    private String parse_access_token(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            // Parse access_token from response
            // TODO: Use a JSON parser instead
            return response.toString().split("&")[0].split("=")[1];
        }
    }

    private void handleError(int responseCode, HttpURLConnection conn) throws IOException {
        LOG.error("POST request failed with response code: " + responseCode);
        //System.out.println("POST request failed with response code: " + responseCode);
        //System.out.println(conn.getResponseMessage());
    }


    public String testFunc(Request request) throws IOException {

        String code = request.queryParams("code");
        // print all the request params
        String access_token = getAccessToken(code);

        StringBuilder sb = new StringBuilder();
        sb.append("Access Token:   " + access_token + "    ");
        String userEmail = getUserEmail(access_token);
        sb.append("User Email: " + userEmail + "    ");

        oauthLogin(userEmail);


        return sb.toString();
    }


    private void oauthLogin(String userEmail) {
        if (UserService.ensureExists(userEmail)) {
            // user exists already

        } else {

        };
    }
    public String getUserEmail(String accessToken) throws IOException {
        URL emailURL = new URL(EMAIL_URL);
        HttpURLConnection emailConnection = (HttpURLConnection) emailURL.openConnection();
        emailConnection.setRequestProperty(HTTP_HEADER_AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken);

        try (InputStream response = emailConnection.getInputStream();
             Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();

            // TODO: Use a JSON parser instead of this tomfooery
            // System.out.println(responseBody);
            int primaryIndex = responseBody.indexOf("primary\":true");
            responseBody = responseBody.substring(0, primaryIndex);
            int emailIndex = responseBody.indexOf("email");
            return responseBody.substring(emailIndex + 8, responseBody.length() - 3);
        }
    }

    public String getGitPostParams(String OAuthCode){
        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append("&client_id=" + CLIENT_ID);
        paramBuilder.append("&client_secret=" + CLIENT_SECRET);
        paramBuilder.append("&code=" + OAuthCode);
        paramBuilder.append("&redirect_uri=" + "http://localhost:8000/");
        return paramBuilder.toString();
    }

    // ================== GOOGLE FILTH ==================
    // (for reference)


    public String getPostParams(String OAuthCode){
        // seriously destroy this, and never use google oauth again
        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append("&code=" + OAuthCode);
        paramBuilder.append("&client_id=" + CLIENT_ID);
        paramBuilder.append("&client_secret=" + CLIENT_SECRET);
        paramBuilder.append("&grant_type=authorization_code");
        paramBuilder.append("&redirect_uri=" + "http://localhost:8000/");
        return paramBuilder.toString();
    }
    // email scope: https://www.googleapis.com/auth/userinfo.email



}
