package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.performance_metric.pack.MetricPack;
import com.khartec.waltz.service.performance_metric.PerformanceMetricPackService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class PerformanceMetricPackEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "performance-metric", "pack");


    private final PerformanceMetricPackService service;


    @Autowired
    public PerformanceMetricPackEndpoint(PerformanceMetricPackService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String getByIdPath = mkPath(BASE_URL, "id", ":id");

        ListRoute<EntityReference> findAllRoute = (request, response)
                -> service.findAllPackReferences();
        DatumRoute<MetricPack> getByIdRoute = (request, response)
                -> service.getById(getId(request));

        getForList(findAllPath, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }
}
