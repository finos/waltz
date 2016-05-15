package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.database.Database;
import com.khartec.waltz.model.database.DatabaseSummaryStatistics;
import com.khartec.waltz.service.database.DatabaseService;
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
public class DatabaseEndpoint implements Endpoint {

    private final DatabaseService databaseService;

    private static final String BASE_URL = mkPath("api", "database");

    @Autowired
    public DatabaseEndpoint(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void register() {

        String findForAppPath = mkPath(BASE_URL, "app", ":id");
        String findForAppSelectorPath = mkPath(BASE_URL);
        String findStatsForAppIdSelectorPath = mkPath(BASE_URL, "stats");


        ListRoute<Database> findForAppRoute = (request, response)
                -> databaseService.findByApplicationId(getId(request));

        ListRoute<ApplicationDatabases> findForAppSelectorRoute = (request, response)
                -> databaseService.findByApplicationSelector(readOptionsFromBody(request))
                    .entrySet()
                    .stream()
                    .map(e -> ImmutableApplicationDatabases.builder()
                            .applicationId(e.getKey())
                            .databases(e.getValue())
                            .build())
                    .collect(Collectors.toList());

        DatumRoute<DatabaseSummaryStatistics> findStatsForAppIdSelectorRoute = (request, response)
                -> databaseService.findStatsForAppIdSelector(readOptionsFromBody(request));


        getForList(findForAppPath, findForAppRoute);
        postForList(findForAppSelectorPath, findForAppSelectorRoute);
        postForDatum(findStatsForAppIdSelectorPath, findStatsForAppIdSelectorRoute);

    }
}
