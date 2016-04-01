package com.khartec.waltz.web.endpoints.auth;

import com.auth0.jwt.JWTVerifier;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.halt;

public class DisallowAnonymousFilter implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        JWTVerifier verifier = new JWTVerifier(JWTUtilities.SECRET);
        String authorizationHeader = request.headers("Authorization");

        if (authorizationHeader == null) {
            halt("Anonymous not allowed");
        } else {
            String token = authorizationHeader.replaceFirst("Bearer ", "");
            Map<String, Object> claims = verifier.verify(token);
            AuthenticationUtilities.setUser(request, (String) claims.get("sub"));
        }
    }

}
