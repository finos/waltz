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

import org.finos.waltz.service.entity_statistic.EntityStatisticService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.EntityStatisticQueryOptions;
import org.finos.waltz.common.EnumUtilities;
import org.finos.waltz.model.Duration;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.entity_statistic.EntityStatistic;
import org.finos.waltz.model.entity_statistic.EntityStatisticDefinition;
import org.finos.waltz.model.entity_statistic.EntityStatisticValue;
import org.finos.waltz.model.entity_statistic.RollupKind;
import org.finos.waltz.model.immediate_hierarchy.ImmediateHierarchy;
import org.finos.waltz.model.tally.TallyPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class EntityStatisticEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-statistic");

    private final EntityStatisticService entityStatisticService;

    private static final Logger LOG = LoggerFactory.getLogger(EntityStatisticEndpoint.class);


    @Autowired
    public EntityStatisticEndpoint(EntityStatisticService entityStatisticService) {
        checkNotNull(entityStatisticService, "entityStatisticService cannot be null");
        this.entityStatisticService = entityStatisticService;
    }


    public EntityStatisticQueryOptions readQueryOptionsFromBody(Request request) throws java.io.IOException {
        return readBody(request, EntityStatisticQueryOptions.class);
    }


    private List<TallyPack<String>> findStatTalliesRoute(Request request, Response response) throws IOException {
        EntityStatisticQueryOptions options = readQueryOptionsFromBody(request);
        return entityStatisticService.findStatTallies(options.statisticIds(), options.selector());
    }


    private TallyPack<String> calculateStatTallyRoute(Request request, Response response) throws IOException {
        IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
        RollupKind rollupKind = extractRollupKind(request);
        Long statisticId = getId(request);
        return entityStatisticService.calculateStatTally(statisticId, rollupKind, idSelectionOptions);
    }


    private List<TallyPack<String>> calculateHistoricStatTallyRoute(Request request, Response response) throws IOException {
        IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
        RollupKind rollupKind = extractRollupKind(request);
        Duration duration = EnumUtilities.readEnum(request.queryParams("duration"), Duration.class, s -> Duration.MONTH);
        Long statisticId = getId(request);
        return entityStatisticService.calculateHistoricStatTally(statisticId, rollupKind, idSelectionOptions, duration);
    }


    private RollupKind extractRollupKind(Request request) {
        return readEnum(
                    request,
                    "rollupKind",
                    RollupKind.class,
                    (s) -> {
                        String msg = String.format("rollupKind cannot be [%s]", s);
                        throw new UnsupportedOperationException(msg);
                    });
    }


    @Override
    public void register() {

        String findAllActiveDefinitionsPath = mkPath(BASE_URL, "definition");
        String findDefinitionPath = mkPath(BASE_URL, "definition", ":id");
        String getRelatedStatDefinitionsPath = mkPath(BASE_URL, "definition" , ":statId", "related");
        String findStatsForEntityPath = mkPath(BASE_URL, ":kind", ":id");
        String findStatValuesBySelectorPath = mkPath(BASE_URL, "value", ":statId");
        String findStatAppsBySelectorPath = mkPath(BASE_URL, "app", ":statId");
        String findStatTalliesPath = mkPath(BASE_URL, "tally");
        String calculateStatTallyPath = mkPath(BASE_URL, "tally", ":id", ":rollupKind");
        String calculateHistoricStatTallyPath = mkPath(BASE_URL, "tally", "historic", ":id", ":rollupKind");

        ListRoute<EntityStatisticDefinition> findAllActiveDefinitionsRoute = (request, response)
                -> entityStatisticService.findAllActiveDefinitions(true);

        DatumRoute<EntityStatisticDefinition> findDefinitionRoute = (request, response)
                -> entityStatisticService.getDefinitionById(getId(request));

        ListRoute<EntityStatistic> findStatsForEntityRoute = (request, response)
                -> entityStatisticService.findStatisticsForEntity(getEntityReference(request), true);

        ListRoute<EntityStatisticValue> findStatValuesForAppSelectorRoute = (request, response)
                -> entityStatisticService.getStatisticValuesForAppIdSelector(getLong(request, "statId"), readIdSelectionOptionsFromBody(request));

        ListRoute<Application> findStatAppsForAppSelectorRoute = (request, response)
                -> entityStatisticService.getStatisticAppsForAppIdSelector(getLong(request, "statId"), readIdSelectionOptionsFromBody(request));

        DatumRoute<ImmediateHierarchy<EntityStatisticDefinition>> getRelatedStatDefinitionsRoute = (request, response)
                -> entityStatisticService.getRelatedStatDefinitions(getLong(request, "statId"), true);

        getForList(findAllActiveDefinitionsPath, findAllActiveDefinitionsRoute);
        getForList(findStatsForEntityPath, findStatsForEntityRoute);
        postForList(findStatValuesBySelectorPath, findStatValuesForAppSelectorRoute);
        postForList(findStatAppsBySelectorPath, findStatAppsForAppSelectorRoute);
        postForList(findStatTalliesPath, this::findStatTalliesRoute);
        postForDatum(calculateStatTallyPath, this::calculateStatTallyRoute);
        postForDatum(calculateHistoricStatTallyPath, this::calculateHistoricStatTallyRoute);
        getForDatum(getRelatedStatDefinitionsPath, getRelatedStatDefinitionsRoute);
        getForDatum(findDefinitionPath, findDefinitionRoute);
    }

}
