package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.service.trait.TraitUsageService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class TraitUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "trait-usage");

    private final TraitUsageService service;


    @Autowired
    public TraitUsageEndpoint(TraitUsageService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String findByEntityKindPath = mkPath(BASE_URL, "entity", ":kind");
        String findByEntityReferencePath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByTraitIdPath = mkPath(BASE_URL, "trait", ":id");

        ListRoute<TraitUsage> findAllRoute = (request, response) -> service.findAll();
        ListRoute<TraitUsage> findByEntityKindRoute = (request, response) -> service.findByEntityKind(getKind(request));
        ListRoute<TraitUsage> findByEntityReferenceRoute = (request, response) -> service.findByEntityReference(getEntityReference(request));
        ListRoute<TraitUsage> findByTraitIdRoute = (request, response) -> service.findByTraitId(getId(request));

        getForList(findAllPath, findAllRoute);
        getForList(findByEntityKindPath, findByEntityKindRoute);
        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        getForList(findByTraitIdPath, findByTraitIdRoute);

    }
}
