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
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.khartec.waltz.service.settings.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.UnsupportedEncodingException;


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

        } catch (UnsupportedEncodingException uee) {
            LOG.error("Cannot create JWT Verifier, this is bad", uee);
            throw new UnsupportedOperationException(uee);
        }
    }


    @Override
    public void handle(Request request, Response response) throws Exception {
        String authorizationHeader = request.headers("Authorization");

        if (authorizationHeader == null) {
            AuthenticationUtilities.setUserAsAnonymous(request);
        } else {
            String token = authorizationHeader.replaceFirst("Bearer ", "");
            DecodedJWT decodedToken = JWT.decode(token);

            JWTVerifier verifier = selectVerifier(decodedToken);

            DecodedJWT decodedJWT = verifier.verify(token);
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
