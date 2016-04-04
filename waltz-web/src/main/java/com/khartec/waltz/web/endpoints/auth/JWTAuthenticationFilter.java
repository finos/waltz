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
