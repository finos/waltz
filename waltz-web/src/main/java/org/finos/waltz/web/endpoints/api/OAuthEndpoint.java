package org.finos.waltz.web.endpoints.api;


import org.finos.waltz.service.oauth.OAuthService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class OAuthEndpoint implements Endpoint{

    private static final Logger LOG = LoggerFactory.getLogger(OAuthEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "oauth");
    private final OAuthService OAuthService;
    public OAuthEndpoint(OAuthService OAuthService){

        checkNotNull(OAuthService, "OAuthService cannot be null");
        this.OAuthService = OAuthService;
    }

    @Override
    public void register() {
        String OAuthPath = WebUtilities.mkPath(BASE_URL);



        System.out.println("Trying to register DatumRoute");
        DatumRoute<String> OAuthRoute = (request, response)
                -> OAuthService.testFunc(request);



        EndpointUtilities.getForDatum(OAuthPath, OAuthRoute);
    }
}
