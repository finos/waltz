package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipKindService;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;

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
    }

    private Set<LegalEntityRelationship> findByLegalEntityIdRoute(Request request, Response response) {
        return legalEntityRelationshipService.findByLegalEntityId(getId(request));
    }

    private Set<LegalEntityRelationship> findByRelationshipKindIdRoute(Request request, Response response) {
        return legalEntityRelationshipService.findByRelationshipKindIdRoute(getId(request));
    }

    private Set<LegalEntityRelationship> findByEntityReferenceRoute(Request request, Response response) {
        return legalEntityRelationshipService.findByEntityReference(getEntityReference(request));
    }

}
