package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.legal_entity.LegalEntity;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipKindService;
import org.finos.waltz.service.legal_entity.LegalEntityService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class LegalEntityRelationshipKindEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "legal-entity-relationship-kind");

    private final LegalEntityRelationshipKindService legalEntityRelationshipKindService;

    public LegalEntityRelationshipKindEndpoint(LegalEntityRelationshipKindService legalEntityRelationshipKindService) {
        checkNotNull(legalEntityRelationshipKindService, "legalEntityRelationshipKindService cannot be null");
        this.legalEntityRelationshipKindService = legalEntityRelationshipKindService;
    }

    @Override
    public void register() {

        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute);
        getForList(mkPath(BASE_URL), this::findAllRoute);
    }

    private LegalEntityRelationshipKind getByIdRoute(Request request, Response response) {
        return legalEntityRelationshipKindService.getById(getId(request));
    }

    private Set<LegalEntityRelationshipKind> findAllRoute(Request request, Response response) {
        return legalEntityRelationshipKindService.findAll();
    }

}
