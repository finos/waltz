/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.EnumUtilities;
import com.khartec.waltz.model.Duration;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.EntityStatisticValue;
import com.khartec.waltz.model.entity_statistic.RollupKind;
import com.khartec.waltz.model.immediate_hierarchy.ImmediateHierarchy;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.service.entity_statistic.EntityStatisticService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.EntityStatisticQueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


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
