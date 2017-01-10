/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.software_catalog.SoftwareCatalog;
import com.khartec.waltz.model.software_catalog.SoftwareSummaryStatistics;
import com.khartec.waltz.service.software_catalog.SoftwareCatalogService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class SoftwareCatalogEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "software-catalog");

    private final SoftwareCatalogService service;

    @Autowired
    public SoftwareCatalogEndpoint(SoftwareCatalogService service) {
        this.service = service;
    }


    @Override
    public void register() {

        String findByAppIdsPath = mkPath(BASE_URL, "apps");
        String findStatsForAppIdSelectorPath = mkPath(BASE_URL, "stats");


        DatumRoute<SoftwareCatalog> findByAppIdsRoute = (request, response) ->
                service.findForAppIds(readIdsFromBody(request));

        DatumRoute<SoftwareSummaryStatistics> findStatsForAppIdSelectorRoute = (request, response)
                -> service.findStatisticsForAppIdSelector(readIdSelectionOptionsFromBody(request));


        postForDatum(findByAppIdsPath, findByAppIdsRoute);
        postForDatum(findStatsForAppIdSelectorPath, findStatsForAppIdSelectorRoute);

    }

}
