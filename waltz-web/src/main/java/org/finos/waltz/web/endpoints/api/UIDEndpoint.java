package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;


@Service
public class UIDEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "uid");

    @Override
    public void register() {
        getForDatum(
                mkPath(BASE_URL, "generate-one"),
                (req, resp) -> UUID.randomUUID().toString());
    }
}
