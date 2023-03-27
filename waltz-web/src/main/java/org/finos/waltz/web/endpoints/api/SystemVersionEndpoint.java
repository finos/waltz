package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.service.system_version.SystemVersionService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;

@Service
public class SystemVersionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "system-version");

    private final SystemVersionService systemVersionService;


    @Autowired
    public SystemVersionEndpoint(SystemVersionService systemVersionService) {
        this.systemVersionService = systemVersionService;
    }


    @Override
    public void register() {
        getForDatum(BASE_URL, (req, resp) -> systemVersionService.getVersionInfo());
    }

}
