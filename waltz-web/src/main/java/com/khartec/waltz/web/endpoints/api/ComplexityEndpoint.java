package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class ComplexityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "complexity");

    private final ComplexityRatingService service;


    @Autowired
    public ComplexityEndpoint(ComplexityRatingService service) {
        this.service = service;
    }


    @Override
    public void register() {
        DatumRoute<ComplexityRating> getForApp = (request, response) -> service.getForApp(getId(request));
        ListRoute<ComplexityRating> postForApps = (request, response) -> service.findByAppIds(readBody(request, Long[].class));

        getForDatum(mkPath(BASE_URL, "application", ":id"), getForApp);
        postForList(mkPath(BASE_URL, "apps"), postForApps);
    }
}
