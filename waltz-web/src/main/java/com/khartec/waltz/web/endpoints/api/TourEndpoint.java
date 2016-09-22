package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.tour.TourStep;
import com.khartec.waltz.service.tour.TourService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class TourEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "tour");

    private final TourService tourService;


    @Autowired
    public TourEndpoint(TourService tourService) {
        checkNotNull(tourService, "tourService cannot be null");
        this.tourService = tourService;
    }

    @Override
    public void register() {
        String findByKeyPath = mkPath(BASE_URL, ":key");

        ListRoute<TourStep> findByKeyRoute = (request, response) ->
                tourService.findByKey(request.params("key"));

        getForList(findByKeyPath, findByKeyRoute);
    }
}
