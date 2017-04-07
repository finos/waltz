package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlowSpecDefinitionChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionChangeCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class PhysicalSpecDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition");

    private final UserRoleService userRoleService;

    private final PhysicalSpecDefinitionService specDefinitionService;


    @Autowired
    public PhysicalSpecDefinitionEndpoint(UserRoleService userRoleService,
                                          PhysicalSpecDefinitionService specDefinitionService) {
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(specDefinitionService, "specDefinitionService cannot be null");

        this.userRoleService = userRoleService;
        this.specDefinitionService = specDefinitionService;
    }


    @Override
    public void register() {
        String findForSpecificationPath = mkPath(BASE_URL, "specification", ":id");
        String createPath = mkPath(BASE_URL, "specification", ":id");

        ListRoute<PhysicalSpecDefinition> findForSpecificationRoute =
                (req, res) -> specDefinitionService.findForSpecification(getId(req));

        DatumRoute<Long> createRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.create(
                    getUsername(req),
                    getId(req),
                    readBody(req, PhysicalSpecDefinitionChangeCommand.class));
        };

        getForList(findForSpecificationPath, findForSpecificationRoute);
        postForDatum(createPath, createRoute);
    }
}
