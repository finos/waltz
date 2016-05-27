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
        String getForAppPath = mkPath(BASE_URL, "application", ":id");
        String findForAppIdSelectorPath = BASE_URL;
        String rebuildPath = mkPath(BASE_URL, "rebuild");

        DatumRoute<ComplexityRating> getForAppRoute = (request, response) -> service.getForApp(getId(request));
        ListRoute<ComplexityRating> findForAppIdSelectorRoute = (request, response) -> service.findForAppIdSelector(readOptionsFromBody(request));
        DatumRoute<Integer> rebuildRoute = (request, response) -> service.rebuild();

        getForDatum(getForAppPath, getForAppRoute);
        postForList(findForAppIdSelectorPath, findForAppIdSelectorRoute);
        getForDatum(rebuildPath, rebuildRoute);
    }
}
