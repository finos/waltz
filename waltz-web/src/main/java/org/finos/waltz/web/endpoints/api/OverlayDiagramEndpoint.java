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
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.overlay_diagram.AppCostWidgetDao;
import org.finos.waltz.data.overlay_diagram.AppCountWidgetDao;
import org.finos.waltz.data.overlay_diagram.OverlayDiagramDao;
import org.finos.waltz.model.overlay_diagram.CostWidgetDatum;
import org.finos.waltz.model.overlay_diagram.CountWidgetDatum;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.jooq.Record1;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class OverlayDiagramEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(OverlayDiagramEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "overlay-diagram");

    private final OverlayDiagramDao overlayDiagramDao;
    private AppCountWidgetDao appCountWidgetDao;
    private AppCostWidgetDao appCostWidgetDao;


    @Autowired
    public OverlayDiagramEndpoint(OverlayDiagramDao overlayDiagramDao,
                                  AppCountWidgetDao appCountWidgetDao,
                                  AppCostWidgetDao appCostWidgetDao) {
        checkNotNull(overlayDiagramDao, "overlayDiagramDao must not be null");
        checkNotNull(appCountWidgetDao, "appCountWidgetDao must not be null");
        checkNotNull(appCostWidgetDao, "appCostWidgetDao must not be null");

        this.overlayDiagramDao = overlayDiagramDao;
        this.appCountWidgetDao = appCountWidgetDao;
        this.appCostWidgetDao = appCostWidgetDao;
    }


    @Override
    public void register() {

        String findAppCountWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id",  "app-count-widget");
        String findAppCostWidgetDataPath = mkPath(BASE_URL, "diagram-id", ":id",  "app-cost-widget");

        ListRoute<CountWidgetDatum> findAppCountWidgetDataRoute = (request, response) -> {

            ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
            Select<Record1<Long>> inScopeAppIds = applicationIdSelectorFactory.apply(readIdSelectionOptionsFromBody(request));

            return appCountWidgetDao
                    .findWidgetData(getId(request), inScopeAppIds, DateTimeUtilities.nowUtc().toLocalDate().plusYears(2));
        };

        postForList(findAppCountWidgetDataPath, findAppCountWidgetDataRoute);


        ListRoute<CostWidgetDatum> findAppCostWidgetDataRoute = (request, response) -> {

            ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
            Select<Record1<Long>> inScopeAppIds = applicationIdSelectorFactory.apply(readIdSelectionOptionsFromBody(request));

            return appCostWidgetDao
                    .findWidgetData(getId(request), inScopeAppIds, DateTimeUtilities.nowUtc().toLocalDate().plusYears(2));
        };

        postForList(findAppCostWidgetDataPath, findAppCostWidgetDataRoute);
    }

}
