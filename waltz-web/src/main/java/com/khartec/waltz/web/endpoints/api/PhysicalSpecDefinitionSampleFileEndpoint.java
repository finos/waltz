package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionSampleFileService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;

@Service
public class PhysicalSpecDefinitionSampleFileEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition-sample-file");


    private final PhysicalSpecDefinitionSampleFileService specDefinitionSampleFileService;


    @Autowired
    public PhysicalSpecDefinitionSampleFileEndpoint(
            PhysicalSpecDefinitionSampleFileService specDefinitionSampleFileService) {

        checkNotNull(specDefinitionSampleFileService, "specDefinitionSampleFileService cannot be null");

        this.specDefinitionSampleFileService = specDefinitionSampleFileService;
    }

    @Override
    public void register() {
        String findForSpecDefinitionPath = mkPath(BASE_URL, "spec-definition", ":id");

        DatumRoute<PhysicalSpecDefinitionSampleFile> findForSpecDefinitionRoute =
                (req, res) -> specDefinitionSampleFileService.findForSpecDefinition(getId(req)).orElse(null);

        getForDatum(findForSpecDefinitionPath, findForSpecDefinitionRoute);

    }
}
