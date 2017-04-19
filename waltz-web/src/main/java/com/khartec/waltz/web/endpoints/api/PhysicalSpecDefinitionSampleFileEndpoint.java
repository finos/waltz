package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFileCreateCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionSampleFileService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class PhysicalSpecDefinitionSampleFileEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition-sample-file");

    private final UserRoleService userRoleService;

    private final PhysicalSpecDefinitionSampleFileService specDefinitionSampleFileService;


    @Autowired
    public PhysicalSpecDefinitionSampleFileEndpoint(UserRoleService userRoleService,
                                                    PhysicalSpecDefinitionSampleFileService specDefinitionSampleFileService) {

        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(specDefinitionSampleFileService, "specDefinitionSampleFileService cannot be null");

        this.userRoleService = userRoleService;
        this.specDefinitionSampleFileService = specDefinitionSampleFileService;
    }

    @Override
    public void register() {
        String findForSpecDefinitionPath = mkPath(BASE_URL, "spec-definition", ":id");
        String createPath = mkPath(BASE_URL, "spec-definition", ":id");

        DatumRoute<PhysicalSpecDefinitionSampleFile> findForSpecDefinitionRoute =
                (req, res) -> specDefinitionSampleFileService.findForSpecDefinition(getId(req)).orElse(null);

        DatumRoute<Long> createRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionSampleFileService.create(
                    getId(req),
                    readBody(req, PhysicalSpecDefinitionSampleFileCreateCommand.class));
        };

        getForDatum(findForSpecDefinitionPath, findForSpecDefinitionRoute);
        postForDatum(createPath, createRoute);

    }
}
