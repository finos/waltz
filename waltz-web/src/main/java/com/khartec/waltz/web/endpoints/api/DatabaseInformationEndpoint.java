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

import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.database_information.DatabaseSummaryStatistics;
import com.khartec.waltz.service.database_information.DatabaseInformationService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.ApplicationDatabases;
import com.khartec.waltz.web.json.ImmutableApplicationDatabases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class DatabaseInformationEndpoint implements Endpoint {

    private final DatabaseInformationService databaseInformationService;

    private static final String BASE_URL = mkPath("api", "database");

    @Autowired
    public DatabaseInformationEndpoint(DatabaseInformationService databaseInformationService) {
        this.databaseInformationService = databaseInformationService;
    }

    @Override
    public void register() {

        String findForAppPath = mkPath(BASE_URL, "app", ":id");
        String findForAppSelectorPath = mkPath(BASE_URL);
        String calculateStatsForAppIdSelectorPath = mkPath(BASE_URL, "stats");


        ListRoute<DatabaseInformation> findForAppRoute = (request, response)
                -> databaseInformationService.findByApplicationId(getId(request));

        ListRoute<ApplicationDatabases> findForAppSelectorRoute = (request, response)
                -> databaseInformationService.findByApplicationSelector(readIdSelectionOptionsFromBody(request))
                    .entrySet()
                    .stream()
                    .map(e -> ImmutableApplicationDatabases.builder()
                            .applicationId(e.getKey())
                            .databases(e.getValue())
                            .build())
                    .collect(Collectors.toList());

        DatumRoute<DatabaseSummaryStatistics> calculateStatsForAppIdSelectorRoute = (request, response)
                -> databaseInformationService.calculateStatsForAppIdSelector(readIdSelectionOptionsFromBody(request));


        getForList(findForAppPath, findForAppRoute);
        postForList(findForAppSelectorPath, findForAppSelectorRoute);
        postForDatum(calculateStatsForAppIdSelectorPath, calculateStatsForAppIdSelectorRoute);

    }
}
