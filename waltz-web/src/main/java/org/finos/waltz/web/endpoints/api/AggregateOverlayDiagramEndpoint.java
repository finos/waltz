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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.overlay_diagram.AssessmentRatingsWidgetDatum;
import org.finos.waltz.model.overlay_diagram.BackingEntityWidgetDatum;
import org.finos.waltz.model.overlay_diagram.CostWidgetDatum;
import org.finos.waltz.model.overlay_diagram.CountWidgetDatum;
import org.finos.waltz.service.aggregate_overlay_diagram.AggregateOverlayDiagramService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.time.LocalDate;

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
        String findAppCountWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-count-widget", ":target-date");
        String findTargetAppCostWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "target-app-cost-widget", ":target-date");
        String findAppAssessmentWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "app-assessment-widget", ":assessment-id");
        String findBackingEntityWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id", "backing-entity-widget");

        DatumRoute<AggregateOverlayDiagram> getByIdRoute = (request, response) -> {
            return aggregateOverlayDiagramService.getById(getId(request));
        };


        ListRoute<AggregateOverlayDiagram> findAllRoute = (request, response) -> {
            return aggregateOverlayDiagramService.findAll();
        };


        ListRoute<CountWidgetDatum> findAppCountWidgetDataRoute = (request, response) -> {
            return aggregateOverlayDiagramService
                    .findAppCountWidgetData(
                            getId(request),
                            readIdSelectionOptionsFromBody(request),
                            getTargetDate(request));
        };


        ListRoute<CostWidgetDatum> findTargetAppCostWidgetDataRoute = (request, response) -> {
            return aggregateOverlayDiagramService
                    .findTargetAppCostWidgetData(
                            getId(request),
                            readIdSelectionOptionsFromBody(request),
                            getTargetDate(request));
        };


        ListRoute<AssessmentRatingsWidgetDatum> findAppAssessmentWidgetDataRoute = (request, response) -> {
            long diagramId = getId(request);
            long assessmentId = getLong(request, "assessment-id");
            IdSelectionOptions options = readIdSelectionOptionsFromBody(request);

            return aggregateOverlayDiagramService
                    .findAppAssessmentWidgetData(
                            diagramId,
                            assessmentId,
                            options);
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
        postForList(findAppAssessmentWidgetDataPath, findAppAssessmentWidgetDataRoute);
    }

    private LocalDate getTargetDate(Request request) {
        return getLocalDateParam(request, "target-date")
                .orElse(DateTimeUtilities.nowUtc().toLocalDate().plusYears(2));
    }

}
