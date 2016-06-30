package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.service.data_type_usage.DataTypeUsageService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class DataTypeUsageEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "data-type-usage");

    private final DataTypeUsageService dataTypeUsageService;

    @Autowired
    public DataTypeUsageEndpoint(DataTypeUsageService dataTypeUsageService) {
        Checks.checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        this.dataTypeUsageService = dataTypeUsageService;
    }

    @Override
    public void register() {

        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForDataTypePath = mkPath(BASE_URL, "type", ":type");
        String findForSelectorPath = mkPath(BASE_URL, "selector");


        ListRoute<DataTypeUsage> findForEntityRoute = (request, response)
                -> dataTypeUsageService.findForEntity(getEntityReference(request));

        ListRoute<DataTypeUsage> findForDataTypeRoute = (request, response)
                -> dataTypeUsageService.findForDataType(request.params("type"));

        ListRoute<DataTypeUsage> findForSelectorRoute = (request, response)
                -> dataTypeUsageService.findForIdSelector(EntityKind.APPLICATION, readOptionsFromBody(request));


        getForList(findForEntityPath, findForEntityRoute);
        getForList(findForDataTypePath, findForDataTypeRoute);
        postForList(findForSelectorPath, findForSelectorRoute);

    }
}
