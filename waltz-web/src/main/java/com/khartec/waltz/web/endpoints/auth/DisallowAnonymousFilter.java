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
        } catch (UnsupportedEncodingException uee) {
            LOG.error("Cannot create JWT Verifier, this is bad", uee);
            throw new UnsupportedOperationException(uee);
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
