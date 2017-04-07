package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionFieldChangeCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionFieldService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;
import static java.util.stream.Collectors.toList;

@Service
public class PhysicalSpecDefinitionFieldEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition-field");


    private final UserRoleService userRoleService;

    private final PhysicalSpecDefinitionFieldService specDefinitionFieldService;


    @Autowired
    public PhysicalSpecDefinitionFieldEndpoint(UserRoleService userRoleService,
                                               PhysicalSpecDefinitionFieldService specDefinitionFieldService) {
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(specDefinitionFieldService, "specDefinitionFieldService cannot be null");

        this.userRoleService = userRoleService;
        this.specDefinitionFieldService = specDefinitionFieldService;
    }


    @Override
    public void register() {
        String findForSpecDefinitionPath = mkPath(BASE_URL, "spec-definition", ":id");
        String createFieldsPath = mkPath(BASE_URL, "spec-definition", ":id", "fields");

        ListRoute<PhysicalSpecDefinitionField> findForSpecDefinitionRoute =
                (req, res) -> specDefinitionFieldService.findForSpecDefinition(getId(req));

        ListRoute<Long> createFieldsRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            String userName = getUsername(req);
            long specDefinitionId = getId(req);
            PhysicalSpecDefinitionFieldChangeCommand[] commands = readBody(
                    req,
                    PhysicalSpecDefinitionFieldChangeCommand[].class);

            return Arrays.stream(commands)
                    .map(c -> specDefinitionFieldService.create(userName, specDefinitionId, c))
                    .collect(toList());
        };

        getForList(findForSpecDefinitionPath, findForSpecDefinitionRoute);
        postForList(createFieldsPath, createFieldsRoute);
    }
}
