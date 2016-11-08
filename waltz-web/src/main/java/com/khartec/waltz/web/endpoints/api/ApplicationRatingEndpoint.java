package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.application_rating.ApplicationRating;
import com.khartec.waltz.service.application_rating.ApplicationRatingService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.getKind;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class ApplicationRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "app-rating");


    private final ApplicationRatingService applicationRatingService;


    @Autowired
    public ApplicationRatingEndpoint(ApplicationRatingService applicationRatingService) {
        checkNotNull(applicationRatingService, "applicationRatingService cannot be null");
        this.applicationRatingService = applicationRatingService;
    }


    @Override
    public void register() {

        String findRatingsByIdPath = mkPath(BASE_URL, "perspective", ":kind", "app", ":id");

        ListRoute<ApplicationRating> findRatingsByIdRoute = (request, response)
                -> applicationRatingService.findRatingsById(
                        getKind(request),
                        getId(request));

        getForList(findRatingsByIdPath, findRatingsByIdRoute);

    }
}
