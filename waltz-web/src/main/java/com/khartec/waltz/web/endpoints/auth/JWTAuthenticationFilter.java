package com.khartec.waltz.web.endpoints.auth;

import com.auth0.jwt.JWTVerifier;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.util.Map;


public class JWTAuthenticationFilter implements Filter {

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
