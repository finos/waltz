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
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.khartec.waltz.service.settings.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class DisallowAnonymousFilter extends WaltzFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DisallowAnonymousFilter.class);

    private final JWTVerifier verifier;


    public DisallowAnonymousFilter(SettingsService settingsService) {
        super(settingsService);
        try {
            Algorithm algorithm = Algorithm.HMAC256(JWTUtilities.SECRET);
            verifier = JWT.require(algorithm)
                    .withIssuer(JWTUtilities.ISSUER)
                    .build(); //Reusable verifier instance
        } catch (Exception e) {
            LOG.error("Cannot create JWT Verifier, this is bad", e);
            throw new UnsupportedOperationException(e);
        }
    }


    @Override
    public void handle(Request request, Response response) throws Exception {
        String authorizationHeader = request.headers("Authorization");

        if (authorizationHeader == null) {
            halt("Anonymous not allowed");
        } else {
            String token = authorizationHeader.replaceFirst("Bearer ", "");
            DecodedJWT decodedJWT = verifier.verify(token);
            AuthenticationUtilities.setUser(request, decodedJWT.getSubject());
        }
    }

}
