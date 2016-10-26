package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class PhysicalFlowEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-flow");


    private final PhysicalFlowService physicalFlowService;


    @Autowired
    public PhysicalFlowEndpoint(PhysicalFlowService physicalFlowService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        this.physicalFlowService = physicalFlowService;
    }


    @Override
    public void register() {
        String findByEntityRefPath = mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id");

        String findBySpecificationIdPath = mkPath(
                BASE_URL,
                "specification",
                ":id");

        String findDescibedLineageFlows = mkPath(
                BASE_URL,
                "lineage");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");


        ListRoute<PhysicalFlow> findByEntityRefRoute =
                (request, response) -> physicalFlowService
                        .findByEntityReference(
                            getEntityReference(request));

        ListRoute<PhysicalFlow> findBySpecificationIdRoute =
                (request, response) -> physicalFlowService
                        .findBySpecificationId(
                                getId(request));

        ListRoute<PhysicalFlow> findDescribedLineageFlows =
                (request, response) -> physicalFlowService
                        .findAllDescribedLineageFlows();

        DatumRoute<PhysicalFlow> getByIdRoute =
                (request, response) -> physicalFlowService
                        .getById(getId(request));


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findBySpecificationIdPath, findBySpecificationIdRoute);
        getForList(findDescibedLineageFlows, findDescribedLineageFlows);

    }
}
