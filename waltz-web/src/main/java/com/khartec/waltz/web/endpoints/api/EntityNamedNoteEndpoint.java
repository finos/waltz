package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.entity_named_note.EntityNamedNote;
import com.khartec.waltz.model.entity_named_note.ImmutableEntityNamedNote;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class EntityNamedNoteEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "entity-named-note");

    @Override
    public void register() {
        String findByEntityReferencePath = mkPath(BASE, "entity-ref", ":kind", ":id");

        ListRoute<EntityNamedNote> findByEntityReferenceRoute = (req, res) -> newArrayList(ImmutableEntityNamedNote.builder()
                .entityReference(getEntityReference(req))
                .namedNoteTypeId(1)
                .noteText("Sample note text containing _italics_ and *bold* text")
                .provenance("TEST")
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("admin")
                .build());

        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
    }
}
