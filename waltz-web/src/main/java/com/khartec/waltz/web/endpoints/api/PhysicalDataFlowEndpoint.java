package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_data_flow.PhysicalDataFlow;
import com.khartec.waltz.service.physical_data_flow.PhysicalDataFlowService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class PhysicalDataFlowEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-data-flow");


    private final PhysicalDataFlowService physicalDataFlowService;


    @Autowired
    public PhysicalDataFlowEndpoint(PhysicalDataFlowService physicalDataFlowService) {
        checkNotNull(physicalDataFlowService, "physicalDataFlowService cannot be null");
        this.physicalDataFlowService = physicalDataFlowService;
    }


    @Override
    public void register() {
        String findByEntityRefPath = mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id");

        String findByArticleIdPath = mkPath(
                BASE_URL,
                "article",
                ":id");

        ListRoute<PhysicalDataFlow> findByEntityRefRoute =
                (request, response) -> physicalDataFlowService
                        .findByEntityReference(
                            getEntityReference(request));

        ListRoute<PhysicalDataFlow> findByArticleIdRoute =
                (request, response) -> physicalDataFlowService
                        .findByArticleId(
                                getId(request));

        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findByArticleIdPath, findByArticleIdRoute);
    }
}
