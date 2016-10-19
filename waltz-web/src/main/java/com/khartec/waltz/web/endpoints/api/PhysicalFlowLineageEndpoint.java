package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineage;
import com.khartec.waltz.service.physical_flow_lineage.PhysicalFlowLineageService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class PhysicalFlowLineageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-flow-lineage");


    private final PhysicalFlowLineageService physicalFlowLineageService;


    @Autowired
    public PhysicalFlowLineageEndpoint(PhysicalFlowLineageService physicalFlowLineageService) {
        checkNotNull(physicalFlowLineageService, "physicalFlowLineageService cannot be null");
        this.physicalFlowLineageService = physicalFlowLineageService;
    }


    @Override
    public void register() {
        String findByPhysicalFlowIdPath = mkPath(
                BASE_URL,
                "physical-flow",
                ":id");

        String findContributionsByPhysicalFlowIdPath = mkPath(
                BASE_URL,
                "physical-flow",
                ":id",
                "contributions");

        ListRoute<PhysicalFlowLineage> findContributionsByPhysicalFlowIdRoute =
                (request, response)
                        -> physicalFlowLineageService.findContributionsByPhysicalFlowId(getId(request));

        ListRoute<PhysicalFlowLineage> findByPhysicalFlowIdRoute =
                (request, response)
                        -> physicalFlowLineageService.findByPhysicalFlowId(getId(request));

        getForList(findByPhysicalFlowIdPath, findByPhysicalFlowIdRoute);
        getForList(findContributionsByPhysicalFlowIdPath, findContributionsByPhysicalFlowIdRoute);
    }
}
