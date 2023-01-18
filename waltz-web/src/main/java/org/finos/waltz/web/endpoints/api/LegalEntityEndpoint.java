package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.data.legal_entity.LegalEntityRelationshipDao;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.finos.waltz.service.legal_entity.LegalEntityService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;

@Service
public class LegalEntityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "legal-entity");

    private final LegalEntityService legalEntityService;

    public LegalEntityEndpoint(LegalEntityService legalEntityService) {
        checkNotNull(legalEntityService, "legalEntityService cannot be null");
        this.legalEntityService = legalEntityService;
    }

    @Override
    public void register() {
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute);
    }

    private LegalEntity getByIdRoute(Request request, Response response) {
        return legalEntityService.getById(getId(request));
    }

}
