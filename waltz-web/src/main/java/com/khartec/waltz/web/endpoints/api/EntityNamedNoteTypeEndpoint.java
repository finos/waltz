package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_named_note.EntityNamedNodeType;
import com.khartec.waltz.model.entity_named_note.ImmutableEntityNamedNodeType;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

public class EntityNamedNoteTypeEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "entity-named-note-type");

    @Override
    public void register() {
        String findAllPath = BASE;

        ListRoute<EntityNamedNodeType> findAllRoute = (req, res) -> newArrayList(ImmutableEntityNamedNodeType.builder()
                .addApplicableEntityKinds(EntityKind.CHANGE_INITIATIVE, EntityKind.MEASURABLE)
                .id(1)
                .name("Title 1")
                .description("Description for title 1")
                .build());

        getForList(findAllPath, findAllRoute);
    }
}


