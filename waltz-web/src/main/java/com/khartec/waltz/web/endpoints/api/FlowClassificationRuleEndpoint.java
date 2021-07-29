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

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Entry;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.flow_classification_rule.DiscouragedSource;
import com.khartec.waltz.model.flow_classification_rule.FlowClassificationRule;
import com.khartec.waltz.model.flow_classification_rule.FlowClassificationRuleCreateCommand;
import com.khartec.waltz.model.flow_classification_rule.FlowClassificationRuleUpdateCommand;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.flow_classification_rule.FlowClassificationRuleService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class FlowClassificationRuleEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(FlowClassificationRuleEndpoint.class);
    private static final String BASE_URL = mkPath("api", "flow-classification-rule");

    private final FlowClassificationRuleService flowClassificationRuleService;
    private final UserRoleService userRoleService;


    @Autowired
    public FlowClassificationRuleEndpoint(FlowClassificationRuleService flowClassificationRuleService,
                                          UserRoleService userRoleService) {
        checkNotNull(flowClassificationRuleService, "flowClassificationRuleService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.flowClassificationRuleService = flowClassificationRuleService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // -- PATHS

        String recalculateFlowRatingsPath = mkPath(BASE_URL, "recalculate-flow-ratings");
        String findDiscouragedSourcesPath = mkPath(BASE_URL, "discouraged");
        String findFlowClassificationRulesBySelectorPath = mkPath(BASE_URL, "selector");
        String calculateConsumersForDataTypeIdSelectorPath = mkPath(BASE_URL, "data-type", "consumers");
        String findByEntityReferencePath = mkPath(BASE_URL, "entity-ref", ":kind", ":id");
        String findByApplicationIdPath = mkPath(BASE_URL, "app", ":id");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String deletePath = mkPath(BASE_URL, "id", ":id");
        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");


        // -- ROUTES

        ListRoute<FlowClassificationRule> findByEntityReferenceRoute = (request, response)
                -> flowClassificationRuleService.findByEntityReference(getEntityReference(request));

        ListRoute<FlowClassificationRule> findByApplicationIdRoute = (request, response)
                -> flowClassificationRuleService.findByApplicationId(getId(request));

        ListRoute<DiscouragedSource> findDiscouragedSourcesRoute = (request, response)
                -> flowClassificationRuleService.findDiscouragedSources(readIdSelectionOptionsFromBody(request));

        ListRoute<FlowClassificationRule> findFlowClassificationRulesBySelectorRoute = (request, response)
                -> flowClassificationRuleService.findClassificationRules(readIdSelectionOptionsFromBody(request));

        ListRoute<FlowClassificationRule> findAllRoute = (request, response)
                -> flowClassificationRuleService.findAll();

        DatumRoute<FlowClassificationRule> getByIdRoute = (request, response)
                -> flowClassificationRuleService.getById(getId(request));

        getForDatum(recalculateFlowRatingsPath, this::recalculateFlowRatingsRoute);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForList(calculateConsumersForDataTypeIdSelectorPath, this::calculateConsumersForDataTypeIdSelectorRoute);
        postForList(findDiscouragedSourcesPath, findDiscouragedSourcesRoute);
        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        getForList(findByApplicationIdPath, findByApplicationIdRoute);
        postForList(findFlowClassificationRulesBySelectorPath, findFlowClassificationRulesBySelectorRoute);
        getForList(BASE_URL, findAllRoute);
        putForDatum(BASE_URL, this::updateRoute);
        deleteForDatum(deletePath, this::deleteRoute);
        postForDatum(BASE_URL, this::insertRoute);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested auth source cleanup", username);
        return flowClassificationRuleService.cleanupOrphans(username);
    }


    private String insertRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.AUTHORITATIVE_SOURCE_EDITOR);
        FlowClassificationRuleCreateCommand command = readBody(request, FlowClassificationRuleCreateCommand.class);
        flowClassificationRuleService.insert(command, getUsername(request));
        return "done";
    }


    private String deleteRoute(Request request, Response response) {
        requireRole(userRoleService, request, SystemRole.AUTHORITATIVE_SOURCE_EDITOR);
        long id = getId(request);
        flowClassificationRuleService.remove(id, getUsername(request));
        return "done";
    }


    private String updateRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.AUTHORITATIVE_SOURCE_EDITOR);
        FlowClassificationRuleUpdateCommand command = readBody(request, FlowClassificationRuleUpdateCommand.class);
        flowClassificationRuleService.update(command, getUsername(request));
        return "done";
    }


    private boolean recalculateFlowRatingsRoute(Request request, Response response) {
        requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = getUsername(request);
        LOG.info("Recalculating all flow ratings (requested by: {})", username);

        return flowClassificationRuleService.fastRecalculateAllFlowRatings();
    }


    private List<Entry<EntityReference, Collection<EntityReference>>> calculateConsumersForDataTypeIdSelectorRoute(
            Request request,
            Response response) throws IOException {

        IdSelectionOptions options = readIdSelectionOptionsFromBody(request);

        Map<EntityReference, Collection<EntityReference>> result = flowClassificationRuleService
                .calculateConsumersForDataTypeIdSelector(options);

        return simplifyMapToList(result);
    }

}
