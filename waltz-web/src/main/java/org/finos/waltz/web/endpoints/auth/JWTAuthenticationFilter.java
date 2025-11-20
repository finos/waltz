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
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.finos.waltz.service.settings.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;


/**
 * Authentication filter which verifies a jwt token.  We only care
 * about the bearer name.
 */
public class JWTAuthenticationFilter extends WaltzFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private final JWTVerifier verifier256;
    private final JWTVerifier verifier512;


    public JWTAuthenticationFilter(SettingsService settingsService) {
        super(settingsService);
        try {
            Algorithm algorithm256 = Algorithm.HMAC256(JWTUtilities.SECRET);
            Algorithm algorithm512 = Algorithm.HMAC512(JWTUtilities.SECRET);

            verifier256 = mkVerifier(algorithm256);
            verifier512 = mkVerifier(algorithm512);

        } catch (Exception e) {
            LOG.error("Cannot create JWT Verifier, this is bad", e);
            throw new UnsupportedOperationException(e);
        }
    }


    @Override
    public void handle(Request request, Response response) throws Exception {
        String authorizationHeader = request.headers("Authorization");
        LOG.info("authorizationHeader : {} ", authorizationHeader);

        if (authorizationHeader == null) {
            AuthenticationUtilities.setUserAsAnonymous(request);
        } else {
            String token = authorizationHeader.replaceFirst("Bearer ", "");
            DecodedJWT decodedToken = JWT.decode(token);
            LOG.info("decodedToken : {} ", decodedToken);

            JWTVerifier verifier = selectVerifier(decodedToken);
            LOG.info("verifier : {} ", verifier);

            DecodedJWT decodedJWT = verifier.verify(token);
            LOG.info("decodedJWT : {} ", decodedJWT);
            LOG.info("subject : {} ", decodedJWT.getSubject());

            AuthenticationUtilities.setUser(request, decodedJWT.getSubject());
        }
    }


    private JWTVerifier mkVerifier(Algorithm algorithm) {
        return JWT
                .require(algorithm)
                .withIssuer(JWTUtilities.ISSUER)
                .build();
    }


    private JWTVerifier selectVerifier(DecodedJWT decodedToken) {
        String algorithm = decodedToken.getAlgorithm();
        LOG.info("algorithm : {} ", algorithm);

        switch (algorithm) {
            case "HS256":
                return verifier256;
            case "HS512":
                return verifier512;
            default:
                throw new IllegalStateException("Cannot verify against algorithm: " + algorithm);
        }
    }

}
