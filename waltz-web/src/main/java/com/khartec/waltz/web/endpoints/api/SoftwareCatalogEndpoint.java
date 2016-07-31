package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.software_catalog.SoftwareCatalog;
import com.khartec.waltz.model.software_catalog.SoftwareSummaryStatistics;
import com.khartec.waltz.service.software_catalog.SoftwareCatalogService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class SoftwareCatalogEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "software-catalog");

    private final SoftwareCatalogService service;

    @Autowired
    public SoftwareCatalogEndpoint(SoftwareCatalogService service) {
        this.service = service;
    }


    @Override
    public void register() {

        String findByAppIdsPath = mkPath(BASE_URL, "apps");
        String findStatsForAppIdSelectorPath = mkPath(BASE_URL, "stats");


        DatumRoute<SoftwareCatalog> findByAppIdsRoute = (request, response) ->
                service.findForAppIds(readIdsFromBody(request));

        DatumRoute<SoftwareSummaryStatistics> findStatsForAppIdSelectorRoute = (request, response)
                -> service.findStatisticsForAppIdSelector(readIdSelectionOptionsFromBody(request));


        postForDatum(findByAppIdsPath, findByAppIdsRoute);
        postForDatum(findStatsForAppIdSelectorPath, findStatsForAppIdSelectorRoute);

    }

}
