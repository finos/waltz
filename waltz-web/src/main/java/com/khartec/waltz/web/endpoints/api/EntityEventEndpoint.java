package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.entity_event.EntityEvent;
import com.khartec.waltz.service.entity_event.EntityEventService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class EntityEventEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-event");


    private final EntityEventService entityEventService;


    @Autowired
    public EntityEventEndpoint(EntityEventService entityEventService) {
        this.entityEventService = entityEventService;
    }


    @Override
    public void register() {
        String findByEntityReferencePath = mkPath(BASE_URL, "entity-ref", ":kind", ":id");

        ListRoute<EntityEvent> findByEntityReferenceRoute = (req, res)
                -> entityEventService.findByEntityReference(getEntityReference(req));

        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
    }
}
