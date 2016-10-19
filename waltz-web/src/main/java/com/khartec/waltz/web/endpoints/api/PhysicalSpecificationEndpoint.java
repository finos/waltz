package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class PhysicalSpecificationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-specification");

    private final PhysicalSpecificationService specificationService;


    @Autowired
    public PhysicalSpecificationEndpoint(PhysicalSpecificationService specificationService) {
        checkNotNull(specificationService, "specificationService cannot be null");
        this.specificationService = specificationService;
    }


    @Override
    public void register() {
        String findByProducerAppPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id",
                "produces");

        String findByConsumerAppIdPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id",
                "consumes");

        String findByAppPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id");

        String findBySelectorPath = mkPath(
                BASE_URL,
                "selector");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        ListRoute<PhysicalSpecification> findByProducerAppRoute =
                (request, response) -> specificationService.findByProducer(getEntityReference(request));

        ListRoute<PhysicalSpecification> findByConsumerAppIdRoute =
                (request, response) -> specificationService.findByConsumer(getEntityReference(request));

        ListRoute<PhysicalSpecification> findBySelectorRoute =
                (request, response) -> specificationService.findBySelector(readIdSelectionOptionsFromBody(request));

        DatumRoute<ProduceConsumeGroup<PhysicalSpecification>> findByAppRoute =
                (request, response) -> specificationService.findByEntityReference(getEntityReference(request));

        DatumRoute<PhysicalSpecification> getByIdRoute =
                (request, response) -> specificationService.getById(getId(request));

        getForList(findByProducerAppPath, findByProducerAppRoute);
        getForList(findByConsumerAppIdPath, findByConsumerAppIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);

        getForDatum(findByAppPath, findByAppRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }
}
