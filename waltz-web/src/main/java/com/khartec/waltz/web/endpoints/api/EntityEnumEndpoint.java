package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.entity_enum.EntityEnumDefinition;
import com.khartec.waltz.model.entity_enum.EntityEnumValue;
import com.khartec.waltz.service.entity_enum.EntityEnumService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class EntityEnumEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-enum");


    private final EntityEnumService entityEnumService;


    @Autowired
    public EntityEnumEndpoint(EntityEnumService entityEnumService) {
        this.entityEnumService = entityEnumService;
    }


    @Override
    public void register() {
        String findDefinitionsByEntityKindPath = mkPath(BASE_URL, "definition", ":kind");
        String findValuesByEntityPath = mkPath(BASE_URL, "value", ":kind", ":id");

        ListRoute<EntityEnumDefinition> findDefinitionsByEntityKindRoute =
                (req, res) -> entityEnumService.findDefinitionsByEntityKind(getKind(req));

        ListRoute<EntityEnumValue> findValuesByEntityRoute =
                (req, res) -> entityEnumService.findValuesByEntity(getEntityReference(req));

        getForList(findDefinitionsByEntityKindPath, findDefinitionsByEntityKindRoute);
        getForList(findValuesByEntityPath, findValuesByEntityRoute);
    }
}
