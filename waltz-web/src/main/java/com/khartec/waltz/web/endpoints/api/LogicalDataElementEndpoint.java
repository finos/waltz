package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import com.khartec.waltz.service.logical_data_element.LogicalDataElementService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class LogicalDataElementEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalDataElementEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "logical-data-element");

    private final LogicalDataElementService service;


    public LogicalDataElementEndpoint(LogicalDataElementService service) {
        checkNotNull(service, "service cannot be null");

        this.service = service;
    }


    @Override
    public void register() {
        // read
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute);
        getForDatum(mkPath(BASE_URL, "external-id", ":extId"), this::getByExternalIdRoute);
        getForList(mkPath(BASE_URL, "all"), (request, response) -> service.findAll());
        postForList(mkPath(BASE_URL, "selector"), this::findBySelectorRoute);
    }


    private LogicalDataElement getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }


    private LogicalDataElement getByExternalIdRoute(Request request, Response response) {
        String extId = request.params("extId");
        return service.getByExternalId(extId);
    }


    private List<LogicalDataElement> findBySelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
        return service.findBySelector(idSelectionOptions);
    }

}
