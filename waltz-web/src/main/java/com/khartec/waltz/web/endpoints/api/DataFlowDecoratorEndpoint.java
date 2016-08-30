package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getKind;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

/**
 * Created by dwatkins on 30/08/2016.
 */
@Service
public class DataFlowDecoratorEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-flow-decorator");


    private final DataFlowDecoratorService dataFlowDecoratorService;


    @Autowired
    public DataFlowDecoratorEndpoint(DataFlowDecoratorService dataFlowDecoratorService) {
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        this.dataFlowDecoratorService = dataFlowDecoratorService;
    }


    @Override
    public void register() {

        String findDecorationsBySelectorAndKindPath = mkPath(BASE_URL, "kind", ":kind");

        ListRoute<DataFlowDecorator> findDecorationsBySelectorAndKindRoute =
                (request, response) -> dataFlowDecoratorService
                        .findBySelectorAndKind(
                                readIdSelectionOptionsFromBody(request),
                                getKind(request));

        postForList(
                findDecorationsBySelectorAndKindPath,
                findDecorationsBySelectorAndKindRoute);
    }
}
