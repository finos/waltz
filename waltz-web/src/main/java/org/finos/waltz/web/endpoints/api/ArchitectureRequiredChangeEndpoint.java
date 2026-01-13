package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.architecture_required_change.ArchitectureRequiredChange;
import org.finos.waltz.service.architecture_required_change.ArchitectureRequiredChangeService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.getEntityReference;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class ArchitectureRequiredChangeEndpoint implements Endpoint {
    private static final String BASE_URL = mkPath("api", "architecture-required-change");

    private final ArchitectureRequiredChangeService architectureRequiredChangeService;

    @Autowired
    public ArchitectureRequiredChangeEndpoint(ArchitectureRequiredChangeService architectureRequiredChangeService) {
        this.architectureRequiredChangeService = architectureRequiredChangeService;
    }

    @Override
    public void register() {
        String findForLinkedEntityPath = mkPath(BASE_URL, "linked-entity", ":kind", ":id");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");

        ListRoute<ArchitectureRequiredChange> findForLinkedEntityRoute = (request, response)
                -> architectureRequiredChangeService.findForLinkedEntity(getEntityReference(request));

        DatumRoute<ArchitectureRequiredChange> getByIdRoute = (request, response)
                -> architectureRequiredChangeService.getById(getId(request));

        getForList(findForLinkedEntityPath, findForLinkedEntityRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }

}
