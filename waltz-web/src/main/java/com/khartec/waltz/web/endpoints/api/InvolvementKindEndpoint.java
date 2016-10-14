package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.involvement_kind.InvolvementKindService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class InvolvementKindEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "involvement-kind");

    private final InvolvementKindService service;

    @Autowired
    public InvolvementKindEndpoint(InvolvementKindService service) {
        checkNotNull(service, "service must not be null");

        this.service = service;
    }


    @Override
    public void register() {
        EndpointUtilities.getForList(BASE_URL, (request, response) -> service.findAll());
    }

}
