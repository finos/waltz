package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionFieldService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class PhysicalSpecDefinitionFieldEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition-field");


    private final PhysicalSpecDefinitionFieldService specDefinitionFieldService;


    @Autowired
    public PhysicalSpecDefinitionFieldEndpoint(PhysicalSpecDefinitionFieldService specDefinitionFieldService) {
        checkNotNull(specDefinitionFieldService, "specDefinitionFieldService cannot be null");

        this.specDefinitionFieldService = specDefinitionFieldService;
    }


    @Override
    public void register() {
        String findForSpecDefinitionPath = mkPath(BASE_URL, "spec-definition", ":id");

        ListRoute<PhysicalSpecDefinitionField> findForSpecDefinitionRoute =
                (req, res) -> specDefinitionFieldService.findForSpecDefinition(getId(req));

        getForList(findForSpecDefinitionPath, findForSpecDefinitionRoute);
    }
}
