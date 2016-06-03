package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.process.Process;
import com.khartec.waltz.service.process.ProcessService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


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

        ListRoute<Process> findAllRoute = (request, response)
                -> service.findAll();
        DatumRoute<Process> getByIdRoute = (request, response)
                -> service.getById(getId(request));

        getForList(findAllPath, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }
}
