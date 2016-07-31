package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.maybe;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class DataTypeUsageEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "data-type-usage");

    private final DataTypeUsageService dataTypeUsageService;
    private final UserRoleService userRoleService;
    private final ChangeLogService changeLogService;

    @Autowired
    public DataTypeUsageEndpoint(DataTypeUsageService dataTypeUsageService,
                                 UserRoleService userRoleService,
                                 ChangeLogService changeLogService) {
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.dataTypeUsageService = dataTypeUsageService;
        this.userRoleService = userRoleService;
        this.changeLogService = changeLogService;
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
                -> dataTypeUsageService.findForIdSelector(EntityKind.APPLICATION, readIdSelectionOptionsFromBody(request));

        ListRoute<DataTypeUsage> saveRoute = (request, response)
                -> {
            requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

            String user = getUsername(request);
            EntityReference ref = getEntityReference(request);
            String dataTypeCode = request.params("type");
            UsageInfo[] usages = readBody(request, UsageInfo[].class);

            SystemChangeSet<UsageInfo, UsageKind> changes = dataTypeUsageService.save(ref, dataTypeCode, newArrayList(usages));

            logChanges(user, ref, dataTypeCode, changes);

            return dataTypeUsageService.findForEntityAndDataType(ref, dataTypeCode);
        };

        getForList(findForEntityPath, findForEntityRoute);
        getForList(findForDataTypePath, findForDataTypeRoute);
        postForList(findForSelectorPath, findForSelectorRoute);
        postForList(savePath, saveRoute);
    }


    private void logChanges(String user, EntityReference ref, String dataTypeCode, SystemChangeSet<UsageInfo, UsageKind> changes) {
        maybe(changes.deletes(), deletes -> logDeletes(user, ref, dataTypeCode, deletes));
        maybe(changes.updates(), updates -> logUpdates(user, ref, dataTypeCode, updates));
        maybe(changes.inserts(), inserts -> logInserts(user, ref, dataTypeCode, inserts));
    }


    private void logDeletes(String user, EntityReference ref, String dataTypeCode, Collection<UsageKind> deletes) {
        String message = "Deleted usage kind/s: " + deletes + " for data type: " + dataTypeCode;
        logChange(user, ref, message);
    }


    private void logInserts(String user, EntityReference ref, String dataTypeCode, Collection<UsageInfo> inserts) {
        Collection<UsageKind> kinds = CollectionUtilities.map(inserts, u -> u.kind());
        String message = "Inserted usage kind/s: " + kinds + " for data type: " + dataTypeCode;
        logChange(user, ref, message);
    }


    private void logUpdates(String user, EntityReference ref, String dataTypeCode, Collection<UsageInfo> updates) {
        Collection<UsageKind> kinds = CollectionUtilities.map(updates, u -> u.kind());
        String message = "Updated usage kind/s: " + kinds + " for data type: " + dataTypeCode;
        logChange(user, ref, message);
    }


    private void logChange(String userId, EntityReference ref, String message) {
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .build();

        changeLogService.write(logEntry);
    }
}
