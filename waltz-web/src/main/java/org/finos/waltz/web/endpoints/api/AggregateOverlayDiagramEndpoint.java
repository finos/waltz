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

import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramInfo;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.*;
import org.finos.waltz.service.aggregate_overlay_diagram.AggregateOverlayDiagramService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class AggregateOverlayDiagramEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateOverlayDiagramEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "aggregate-overlay-diagram");

    private final AggregateOverlayDiagramService aggregateOverlayDiagramService;


    @Autowired
    public AggregateOverlayDiagramEndpoint(AggregateOverlayDiagramService aggregateOverlayDiagramService) {
        checkNotNull(aggregateOverlayDiagramService, "aggregateOverlayDiagramService must not be null");

        this.aggregateOverlayDiagramService = aggregateOverlayDiagramService;
    }


    @Override
    public void register() {

        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findAllPath = mkPath(BASE_URL, "all");
        String findAppCountWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-count-widget");
        String findTargetAppCostWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "target-app-cost-widget");
        String findAppCostWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-cost-widget");
        String findAppAssessmentWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-assessment-widget");
        String findAggregatedEntitiesWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "aggregated-entities-widget");
        String findBackingEntityWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "backing-entity-widget");

        DatumRoute<AggregateOverlayDiagramInfo> getByIdRoute = (request, response) -> {
            return aggregateOverlayDiagramService.getById(getId(request));
        };


        ListRoute<AggregateOverlayDiagram> findAllRoute = (request, response) -> {
            return aggregateOverlayDiagramService.findAll();
        };


        ListRoute<CountWidgetDatum> findAppCountWidgetDataRoute = (request, response) -> {

            OverlayDiagramWidgetInfo<AppCountWidgetParameters> widgetInfo = readBody(request, OverlayDiagramAppCountWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .findAppCountWidgetData(
                            getId(request),
                            widgetInfo.idSelectionOptions(),
                            widgetInfo.assessmentBasedSelectionFilter(),
                            widgetInfo.overlayParameters());
        };


        ListRoute<TargetCostWidgetDatum> findTargetAppCostWidgetDataRoute = (request, response) -> {

            OverlayDiagramWidgetInfo<TargetAppCostWidgetParameters> widgetParameters = readBody(request, OverlayDiagramTargetAppCostWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .findTargetAppCostWidgetData(
                            getId(request),
                            widgetParameters.idSelectionOptions(),
                            widgetParameters.assessmentBasedSelectionFilter(),
                            widgetParameters.overlayParameters())
                    ;
        };


        ListRoute<CostWidgetDatum> findAppCostWidgetDataRoute = (request, response) -> {
            OverlayDiagramWidgetInfo<AppCostWidgetParameters> appCostWidgetParameters = readBody(request, OverlayDiagramAppCostWidgetInfo.class);

            return aggregateOverlayDiagramService
                    .findAppCostWidgetData(
                            getId(request),
                            appCostWidgetParameters.assessmentBasedSelectionFilter(),
                            appCostWidgetParameters.idSelectionOptions(),
                            appCostWidgetParameters.overlayParameters());
        };


        ListRoute<AssessmentRatingsWidgetDatum> findAppAssessmentWidgetDataRoute = (request, response) -> {
            long diagramId = getId(request);
            OverlayDiagramWidgetInfo<AssessmentWidgetParameters> widgetParameters = readBody(request, OverlayDiagramAssessmentWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .findAppAssessmentWidgetData(
                            diagramId,
                            widgetParameters.assessmentBasedSelectionFilter(),
                            widgetParameters.idSelectionOptions(),
                            widgetParameters.overlayParameters());
        };


        ListRoute<AggregatedEntitiesWidgetDatum> findAggregatedEntitiesWidgetDataRoute = (request, response) -> {
            OverlayDiagramWidgetInfo<AggregatedEntitiesWidgetParameters> widgetParameters = readBody(request, OverlayDiagramAggregatedEntitiesWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .findAggregatedEntitiesWidgetData(
                            getId(request),
                            widgetParameters.assessmentBasedSelectionFilter(),
                            widgetParameters.idSelectionOptions());
        };


        ListRoute<BackingEntityWidgetDatum> findBackingEntityWidgetDataRoute = (request, response) -> {
            long diagramId = getId(request);
            return aggregateOverlayDiagramService.findBackingEntityWidgetData(diagramId);
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findAllPath, findAllRoute);
        getForList(findBackingEntityWidgetDataPath, findBackingEntityWidgetDataRoute);
        postForList(findAppCountWidgetDataPath, findAppCountWidgetDataRoute);
        postForList(findTargetAppCostWidgetDataPath, findTargetAppCostWidgetDataRoute);
        postForList(findAppCostWidgetDataPath, findAppCostWidgetDataRoute);
        postForList(findAppAssessmentWidgetDataPath, findAppAssessmentWidgetDataRoute);
        postForList(findAggregatedEntitiesWidgetDataPath, findAggregatedEntitiesWidgetDataRoute);
    }

}
