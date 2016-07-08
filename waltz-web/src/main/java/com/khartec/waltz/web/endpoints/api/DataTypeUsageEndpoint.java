package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class DataTypeUsageEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "data-type-usage");

    private final DataTypeUsageService dataTypeUsageService;
    private final UserRoleService userRoleService;

    @Autowired
    public DataTypeUsageEndpoint(DataTypeUsageService dataTypeUsageService, UserRoleService userRoleService) {
        Checks.checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        Checks.checkNotNull(userRoleService, "userRoleService cannot be null");
        this.dataTypeUsageService = dataTypeUsageService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForDataTypePath = mkPath(BASE_URL, "type", ":type");
        String findForSelectorPath = mkPath(BASE_URL, "selector");
        String savePath = mkPath(BASE_URL, "entity", ":kind", ":id", ":type");


        ListRoute<DataTypeUsage> findForEntityRoute = (request, response)
                -> dataTypeUsageService.findForEntity(getEntityReference(request));

        ListRoute<DataTypeUsage> findForDataTypeRoute = (request, response)
                -> dataTypeUsageService.findForDataType(request.params("type"));

        ListRoute<DataTypeUsage> findForSelectorRoute = (request, response)
                -> dataTypeUsageService.findForIdSelector(EntityKind.APPLICATION, readOptionsFromBody(request));

        ListRoute<DataTypeUsage> saveRoute = (request, response)
                -> {
            requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
            EntityReference ref = getEntityReference(request);
            String dataTypeCode = request.params("type");
            UsageInfo[] usages = readBody(request, UsageInfo[].class);

            SystemChangeSet<UsageInfo, UsageKind> changes = dataTypeUsageService.save(ref, dataTypeCode, newArrayList(usages));

            // TODO: update change log with [changes]
            return dataTypeUsageService.findForEntityAndDataType(ref, dataTypeCode);
        };

        getForList(findForEntityPath, findForEntityRoute);
        getForList(findForDataTypePath, findForDataTypeRoute);
        postForList(findForSelectorPath, findForSelectorRoute);
        postForList(savePath, saveRoute);


    }
}
