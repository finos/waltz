/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.auth0.jwt.JWTVerifier;
import com.khartec.waltz.service.settings.SettingsService;
import spark.Request;
import spark.Response;

import java.util.Map;


/**
 * Authentication filter which verifies a jwt token.  We only care
 * about the bearer name.
 */
public class JWTAuthenticationFilter extends WaltzFilter {

    public JWTAuthenticationFilter(SettingsService settingsService) {
        super(settingsService);
    }


    @Override
    public void handle(Request request, Response response) throws Exception {
        JWTVerifier verifier = new JWTVerifier(JWTUtilities.SECRET);
        String authorizationHeader = request.headers("Authorization");

        if (authorizationHeader == null) {
            AuthenticationUtilities.setUserAsAnonymous(request);
        } else {
            String token = authorizationHeader.replaceFirst("Bearer ", "");
            Map<String, Object> claims = verifier.verify(token);
            AuthenticationUtilities.setUser(request, (String) claims.get("sub"));
        }
    }

}
