package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.trait.Trait;
import com.khartec.waltz.service.trait.TraitService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdsFromBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class TraitEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "trait");
    private final TraitService service;


    @Autowired
    public TraitEndpoint(TraitService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByIdsPath = mkPath(BASE_URL, "id");

        ListRoute<Trait> findAllRoute = (request, response) -> service.findAll();
        DatumRoute<Trait> getByIdRoute = (request, response) -> service.getById(getId(request));
        ListRoute<Trait> findByIdsRoute = (request, response) -> service.findByIds(readIdsFromBody(request));

        getForList(findAllPath, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForList(findByIdsPath, findByIdsRoute);
    }

}
