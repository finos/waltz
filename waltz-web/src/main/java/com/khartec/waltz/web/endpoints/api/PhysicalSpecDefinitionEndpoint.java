package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class PhysicalSpecDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition");

    private final PhysicalSpecDefinitionService specDefinitionService;


    @Autowired
    public PhysicalSpecDefinitionEndpoint(PhysicalSpecDefinitionService specDefinitionService) {
        checkNotNull(specDefinitionService, "specDefinitionService cannot be null");

        this.specDefinitionService = specDefinitionService;
    }


    @Override
    public void register() {
        String findForSpecificationPath = mkPath(BASE_URL, "specification", ":id");

        ListRoute<PhysicalSpecDefinition> findForSpecificationRoute =
                (req, res) -> specDefinitionService.findForSpecification(getId(req));

        getForList(findForSpecificationPath, findForSpecificationRoute);
    }
}
