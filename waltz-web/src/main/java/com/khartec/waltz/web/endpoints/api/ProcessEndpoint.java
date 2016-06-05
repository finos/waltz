package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.process.Process;
import com.khartec.waltz.service.process.ProcessService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class ProcessEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "process");


    private final ProcessService service;


    @Autowired
    public ProcessEndpoint(ProcessService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findSupportingCapabilitiesPath = mkPath(BASE_URL, "id", ":id", "capabilities");
        String findForCapabilityPath = mkPath(BASE_URL, "capability", ":id");
        String findForApplicationPath = mkPath(BASE_URL, "application", ":id");

        ListRoute<Process> findAllRoute = (request, response)
                -> service.findAll();

        ListRoute<Process> findForCapabilityRoute = (request, response)
                -> service.findForCapability(getId(request));

        ListRoute<Process> findForApplicationRoute = (request, response)
                -> service.findForApplication(getId(request));

        ListRoute<Capability> findSupportingCapabilitiesRoute = (request, response)
                -> service.findSupportingCapabilitiesRoute(getId(request));

        DatumRoute<Process> getByIdRoute = (request, response)
                -> service.getById(getId(request));

        getForList(findAllPath, findAllRoute);
        getForList(findForCapabilityPath, findForCapabilityRoute);
        getForList(findForApplicationPath, findForApplicationRoute);
        getForList(findSupportingCapabilitiesPath, findSupportingCapabilitiesRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }
}
