package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import com.khartec.waltz.service.entity_svg_diagram.EntitySvgDiagramService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class EntitySvgDiagramEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-svg-diagram");
    private final EntitySvgDiagramService entitySvgDiagramService;


    @Autowired
    public EntitySvgDiagramEndpoint(EntitySvgDiagramService entitySvgDiagramService) {
        checkNotNull(entitySvgDiagramService, "entitySvgDiagramService cannot be null");
        this.entitySvgDiagramService = entitySvgDiagramService;
    }


    @Override
    public void register() {

        String findByEntityPath = mkPath(BASE_URL, "entity-ref", ":kind", ":id");

        ListRoute<EntitySvgDiagram> findByEntityRoute = (request, response) ->
                entitySvgDiagramService.findForEntityReference(getEntityReference(request));

        getForList(findByEntityPath, findByEntityRoute);
    }

}
