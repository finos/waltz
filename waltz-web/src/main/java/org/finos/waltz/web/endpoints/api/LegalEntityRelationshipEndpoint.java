package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipView;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class LegalEntityRelationshipEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "legal-entity-relationship");

    private final LegalEntityRelationshipService legalEntityRelationshipService;

    public LegalEntityRelationshipEndpoint(LegalEntityRelationshipService legalEntityRelationshipService) {
        checkNotNull(legalEntityRelationshipService, "legalEntityRelationshipService cannot be null");
        this.legalEntityRelationshipService = legalEntityRelationshipService;
    }

    @Override
    public void register() {

        getForList(mkPath(BASE_URL, "legal-entity-id", ":id"), this::findByLegalEntityIdRoute);
        getForList(mkPath(BASE_URL, "relationship-kind", ":id"), this::findByRelationshipKindIdRoute);
        getForList(mkPath(BASE_URL, "kind", ":kind", "id", ":id"), this::findByEntityReferenceRoute);
        postForDatum(mkPath(BASE_URL, "relationship-kind", ":id", "view"), this::getViewByRelKindAndSelectorRoute);
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute);
    }

    private LegalEntityRelationship getByIdRoute(Request request, Response response) {
        return legalEntityRelationshipService.getById(getId(request));
    }

    private Set<LegalEntityRelationship> findByLegalEntityIdRoute(Request request, Response response) {
        return legalEntityRelationshipService.findByLegalEntityId(getId(request));
    }

    private Set<LegalEntityRelationship> findByRelationshipKindIdRoute(Request request, Response response) {
        return legalEntityRelationshipService.findByRelationshipKindId(getId(request));
    }

    private Set<LegalEntityRelationship> findByEntityReferenceRoute(Request request, Response response) {
        return legalEntityRelationshipService.findByEntityReference(getEntityReference(request));
    }

    private LegalEntityRelationshipView getViewByRelKindAndSelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
        return legalEntityRelationshipService.getViewByRelKindAndSelector(getId(request), idSelectionOptions);
    }

}
