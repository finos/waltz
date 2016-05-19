package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.source_data_rating.SourceDataRating;
import com.khartec.waltz.service.source_data_rating.SourceDataRatingService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class SourceDataRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "source-data-rating");

    private final SourceDataRatingService service;

    @Autowired
    public SourceDataRatingEndpoint(SourceDataRatingService service) {
        Checks.checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);

        ListRoute<SourceDataRating> findAllRoute = (request, response) ->
                service.findAll();

        getForList(findAllPath, findAllRoute);
    }
}
