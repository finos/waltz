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
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramPreset;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramCreateCommand;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramPresetCreateCommand;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.*;
import org.finos.waltz.model.entity_overlay_diagram.OverlayDiagramKind;
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

import static java.lang.String.format;
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
        String findByKindPath = mkPath(BASE_URL, "diagram-kind", ":kind");
        String getAppCountWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-count-widget");
        String getAttestationWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "attestation");
        String findTargetAppCostWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "target-app-cost-widget");
        String getAppCostWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-cost-widget");
        String getAppAssessmentWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-assessment-widget");
        String getAggregatedEntitiesWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "aggregated-entities-widget");
        String getBackingEntityWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "backing-entity-widget");
        String getComplexityWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "complexity-widget");
        String findPresetsForDiagramPath = mkPath(BASE_URL, "diagram-id", ":id", "presets");
        String createPresetPath = mkPath(BASE_URL, "create-preset");
        String createPath = mkPath(BASE_URL, "create");

        DatumRoute<AggregateOverlayDiagramInfo> getByIdRoute = (request, response) -> {
            return aggregateOverlayDiagramService.getById(getId(request));
        };


        ListRoute<AggregateOverlayDiagram> findAllRoute = (request, response) -> {
            return aggregateOverlayDiagramService.findAll();
        };


        ListRoute<AggregateOverlayDiagram> findByKindRoute = (request, response) -> {
            OverlayDiagramKind overlayDiagramKind = readEnum(request,
                    "kind",
                    OverlayDiagramKind.class,
                    (s) -> null);
            return aggregateOverlayDiagramService.findByKind(overlayDiagramKind);
        };


        DatumRoute<AttestationWidgetData> getAttestationWidgetDataRoute = (request, response) -> {

            OverlayDiagramAttestationWidgetInfo widgetInfo = readBody(request, OverlayDiagramAttestationWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .getAttestationWidgetData(
                            getId(request),
                            widgetInfo.assessmentBasedSelectionFilters(),
                            widgetInfo.idSelectionOptions(),
                            widgetInfo.overlayParameters());
        };

        DatumRoute<CountWidgetData> getAppCountWidgetDataRoute = (request, response) -> {

            OverlayDiagramAppCountWidgetInfo widgetInfo = readBody(request, OverlayDiagramAppCountWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .getAppCountWidgetData(
                            getId(request),
                            widgetInfo.idSelectionOptions(),
                            widgetInfo.assessmentBasedSelectionFilters(),
                            widgetInfo.overlayParameters());
        };


        DatumRoute<TargetCostWidgetData> findTargetAppCostWidgetDataRoute = (request, response) -> {

            OverlayDiagramTargetAppCostWidgetInfo widgetParameters = readBody(request, OverlayDiagramTargetAppCostWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .getTargetAppCostWidgetData(
                            getId(request),
                            widgetParameters.idSelectionOptions(),
                            widgetParameters.assessmentBasedSelectionFilters(),
                            widgetParameters.overlayParameters());
        };


        DatumRoute<CostWidgetData> getAppCostWidgetDataRoute = (request, response) -> {
            OverlayDiagramWidgetInfo<AppCostWidgetParameters> appCostWidgetParameters = readBody(request, OverlayDiagramAppCostWidgetInfo.class);

            return aggregateOverlayDiagramService
                    .getAppCostWidgetData(
                            getId(request),
                            appCostWidgetParameters.assessmentBasedSelectionFilters(),
                            appCostWidgetParameters.idSelectionOptions(),
                            appCostWidgetParameters.overlayParameters());
        };


        DatumRoute<ComplexityWidgetData> getComplexityWidgetDataRoute = (request, response) -> {
            OverlayDiagramWidgetInfo<AppComplexityWidgetParameters> appComplexityWidgetParameters = readBody(request, OverlayDiagramAppComplexityWidgetInfo.class);

            return aggregateOverlayDiagramService
                    .getAppComplexityWidgetData(
                            getId(request),
                            appComplexityWidgetParameters.assessmentBasedSelectionFilters(),
                            appComplexityWidgetParameters.idSelectionOptions(),
                            appComplexityWidgetParameters.overlayParameters());
        };


        DatumRoute<AssessmentRatingsWidgetData> getAppAssessmentWidgetDataRoute = (request, response) -> {
            long diagramId = getId(request);
            OverlayDiagramWidgetInfo<AssessmentWidgetParameters> widgetParameters = readBody(request, OverlayDiagramAssessmentWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .getAppAssessmentWidgetData(
                            diagramId,
                            widgetParameters.assessmentBasedSelectionFilters(),
                            widgetParameters.idSelectionOptions(),
                            widgetParameters.overlayParameters());
        };


        DatumRoute<AggregatedEntitiesWidgetData> getAggregatedEntitiesWidgetDataRoute = (request, response) -> {
            OverlayDiagramWidgetInfo<AggregatedEntitiesWidgetParameters> widgetParameters = readBody(request, OverlayDiagramAggregatedEntitiesWidgetInfo.class, null);

            return aggregateOverlayDiagramService
                    .getAggregatedEntitiesWidgetData(
                            getId(request),
                            widgetParameters.assessmentBasedSelectionFilters(),
                            widgetParameters.idSelectionOptions());
        };


        DatumRoute<BackingEntityWidgetData> getBackingEntityWidgetDataRoute = (request, response) -> {
            long diagramId = getId(request);
            return aggregateOverlayDiagramService.getBackingEntityWidgetData(diagramId);
        };


        ListRoute<AggregateOverlayDiagramPreset> findPresetsForDiagramRoute = (request, response) -> {
            long diagramId = getId(request);
            return aggregateOverlayDiagramService.findPresetsForDiagram(diagramId);
        };


        DatumRoute<Integer> createPresetRoute = (request, response) -> {
            return aggregateOverlayDiagramService.createPreset(readBody(request, OverlayDiagramPresetCreateCommand.class), getUsername(request));
        };

        DatumRoute<Long> createRoute = (request, response) -> {
            return aggregateOverlayDiagramService.create(readBody(request, OverlayDiagramCreateCommand.class), getUsername(request));
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findAllPath, findAllRoute);
        getForList(findByKindPath, findByKindRoute);
        getForDatum(getBackingEntityWidgetDataPath, getBackingEntityWidgetDataRoute);
        postForDatum(getBackingEntityWidgetDataPath, getBackingEntityWidgetDataRoute);
        getForList(findPresetsForDiagramPath, findPresetsForDiagramRoute);
        postForDatum(getAppCountWidgetDataPath, getAppCountWidgetDataRoute);
        postForDatum(getAttestationWidgetDataPath, getAttestationWidgetDataRoute);
        postForDatum(findTargetAppCostWidgetDataPath, findTargetAppCostWidgetDataRoute);
        postForDatum(getAppCostWidgetDataPath, getAppCostWidgetDataRoute);
        postForDatum(getAppAssessmentWidgetDataPath, getAppAssessmentWidgetDataRoute);
        postForDatum(getAggregatedEntitiesWidgetDataPath, getAggregatedEntitiesWidgetDataRoute);
        postForDatum(getComplexityWidgetDataPath, getComplexityWidgetDataRoute);
        postForDatum(createPresetPath, createPresetRoute);
        postForDatum(createPath, createRoute);
    }

}
