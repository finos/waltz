/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.service.usage_info.DataTypeUsageService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.data_type_usage.DataTypeUsage;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.model.usage_info.UsageInfo;
import org.finos.waltz.model.usage_info.UsageKind;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;

@Service
public class DataTypeUsageEndpoint implements Endpoint {


    private static final String BASE_URL = WebUtilities.mkPath("api", "data-type-usage");

    private final DataTypeUsageService dataTypeUsageService;
    private final UserRoleService userRoleService;

    @Autowired
    public DataTypeUsageEndpoint(DataTypeUsageService dataTypeUsageService,
                                 UserRoleService userRoleService) {
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.dataTypeUsageService = dataTypeUsageService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        String findForEntityPath = WebUtilities.mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForDataTypeSelectorPath = WebUtilities.mkPath(BASE_URL, "type");
        String findUsageStatsForDataTypeSelectorPath = WebUtilities.mkPath(BASE_URL, "type", "stats");
        String findForUsageKindByDataTypeSelectorPath = WebUtilities.mkPath(BASE_URL, "usage-kind", ":usage-kind");
        String calculateForAllApplicationsPath = WebUtilities.mkPath(BASE_URL, "calculate-all", "application");
        String findForSelectorPath = WebUtilities.mkPath(BASE_URL, "selector");
        String savePath = WebUtilities.mkPath(BASE_URL, "entity", ":kind", ":id", ":typeId");

        ListRoute<DataTypeUsage> findForEntityRoute = (request, response)
                -> dataTypeUsageService.findForEntity(WebUtilities.getEntityReference(request));

        ListRoute<DataTypeUsage> findForDataTypeSelectorRoute = (request, response)
                -> dataTypeUsageService.findForDataTypeSelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        ListRoute<Tally<String>> findUsageStatsForDataTypeSelectorRoute = (request, response)
                -> dataTypeUsageService.findUsageStatsForDataTypeSelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        ListRoute<DataTypeUsage> findForSelectorRoute = (request, response)
                -> dataTypeUsageService.findForAppIdSelector(EntityKind.APPLICATION, WebUtilities.readIdSelectionOptionsFromBody(request));

        EndpointUtilities.getForList(findForEntityPath, findForEntityRoute);
        EndpointUtilities.postForList(findForDataTypeSelectorPath, findForDataTypeSelectorRoute);
        EndpointUtilities.postForList(findUsageStatsForDataTypeSelectorPath, findUsageStatsForDataTypeSelectorRoute);
        EndpointUtilities.postForList(findForSelectorPath, findForSelectorRoute);
        EndpointUtilities.postForList(savePath, this::saveRoute);
        EndpointUtilities.getForDatum(calculateForAllApplicationsPath, this::calculateForAllApplicationsRoute);
        EndpointUtilities.postForDatum(findForUsageKindByDataTypeSelectorPath, this::findForUsageKindByDataTypeSelectorRoute);
    }


    private Map<Long, Collection<EntityReference>> findForUsageKindByDataTypeSelectorRoute(Request request,
                                                                                           Response response) throws IOException
    {
        IdSelectionOptions options = WebUtilities.readIdSelectionOptionsFromBody(request);
        UsageKind usageKind = WebUtilities.readEnum(request,
                "usage-kind",
                UsageKind.class,
                s -> UsageKind.ORIGINATOR);
        return dataTypeUsageService.findForUsageKindByDataTypeIdSelector(usageKind, options);
    }


    private Boolean calculateForAllApplicationsRoute(Request request,
                                                     Response response) {
        WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
        return dataTypeUsageService.recalculateForAllApplications();
    }


    private List<DataTypeUsage> saveRoute(Request request,
                                          Response response) throws IOException {
        WebUtilities.requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        String user = WebUtilities.getUsername(request);
        EntityReference ref = WebUtilities.getEntityReference(request);
        Long dataTypeId = WebUtilities.getLong(request,"typeId");
        UsageInfo[] usages = WebUtilities.readBody(request, UsageInfo[].class);

        dataTypeUsageService.save(
                ref,
                dataTypeId,
                newArrayList(usages),
                user);

        return dataTypeUsageService.findForEntityAndDataType(
                ref,
                dataTypeId);
    }



}
