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
    private static final String APP_PATH = mkPath(BASE_URL, "app");

    @Autowired
    public DatabaseEndpoint(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void register() {

        String findForAppPath = mkPath(APP_PATH, ":id");
        String findForAppsPath = mkPath(APP_PATH);
        String findStatsForAppsPath = mkPath(APP_PATH, "stats");


        ListRoute<Database> findForAppRoute = (request, response)
                -> databaseService.findByApplicationId(getId(request));

        ListRoute<ApplicationDatabases> findForAppsRoute = (request, response)
                -> databaseService.findByApplicationIds(readIdsFromBody(request))
                    .entrySet()
                    .stream()
                    .map(e -> ImmutableApplicationDatabases.builder()
                            .applicationId(e.getKey())
                            .databases(e.getValue())
                            .build())
                    .collect(Collectors.toList());

        DatumRoute<DatabaseSummaryStatistics> findStatsForAppsRoute = (request, response)
                -> databaseService.findStatsForAppIds(readIdsFromBody(request));


        getForList(findForAppPath, findForAppRoute);
        postForList(findForAppsPath, findForAppsRoute);
        postForDatum(findStatsForAppsPath, findStatsForAppsRoute);

    }
}
