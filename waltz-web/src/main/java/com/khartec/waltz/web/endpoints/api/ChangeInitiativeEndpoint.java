package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.service.change_initiative.ChangeInitiativeService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


public class ChangeInitiativeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "change-initiative");

    private final ChangeInitiativeService service;


    @Autowired
    public ChangeInitiativeEndpoint(ChangeInitiativeService service) {
        Checks.checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {

        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findForRefPath = mkPath(BASE_URL, "ref", ":kind", ":id");

        DatumRoute<ChangeInitiative> getByIdRoute = (request, response) ->
                service.getById(getId(request));

        ListRoute<ChangeInitiative> findForRefRoute = (request, response) ->
                service.findForEntityReference(getEntityReference(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findForRefPath, findForRefRoute);

    }
}
