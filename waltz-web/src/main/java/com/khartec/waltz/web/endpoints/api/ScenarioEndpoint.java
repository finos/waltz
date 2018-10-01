package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.scenario.ImmutableCloneScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.scenario.ScenarioAxisItemService;
import com.khartec.waltz.service.scenario.ScenarioRatingItemService;
import com.khartec.waltz.service.scenario.ScenarioService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.ImmutableFullScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class ScenarioEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "scenario");
    private final RoadmapService roadmapService;
    private final ScenarioService scenarioService;
    private final ScenarioAxisItemService scenarioAxisItemService;
    private final ScenarioRatingItemService scenarioRatingItemService;


    @Autowired
    public ScenarioEndpoint(RoadmapService roadmapService, 
                            ScenarioService scenarioService,
                            ScenarioAxisItemService scenarioAxisItemService,
                            ScenarioRatingItemService scenarioRatingItemService) {
        checkNotNull(roadmapService, "roadmapService cannot be null");
        checkNotNull(scenarioService, "scenarioService cannot be null");
        checkNotNull(scenarioAxisItemService, "scenarioAxisItemService cannot be null");
        checkNotNull(scenarioRatingItemService, "scenarioRatingItemService cannot be null");

        this.roadmapService = roadmapService;
        this.scenarioService = scenarioService;
        this.scenarioAxisItemService = scenarioAxisItemService;
        this.scenarioRatingItemService = scenarioRatingItemService;
    }


    @Override
    public void register() {
        registerFindScenariosForRoadmapId(mkPath(BASE_URL, "by-roadmap-id", ":roadmapId"));
        registerFindScenariosByRoadmapSelector(mkPath(BASE_URL, "by-roadmap-selector"));
        registerGetScenarioById(mkPath(BASE_URL, "id", ":id"));
        registerCloneScenario(mkPath(BASE_URL, "id", ":id", "clone"));
        registerRemoveRating(mkPath(BASE_URL, "id", ":id", "rating", ":appId", ":columnId", ":rowId"));
        registerUpdateRating(mkPath(BASE_URL, "id", ":id", "rating", ":appId", ":columnId", ":rowId", "rating", ":rating"));
        registerAddRating(mkPath(BASE_URL, "id", ":id", "rating", ":appId", ":columnId", ":rowId", ":rating"));
    }

    private void registerUpdateRating(String path) {
        postForDatum(path, (request, response) ->
            scenarioRatingItemService.updateRating(
                    getId(request),
                    getLong(request, "appId"),
                    getLong(request, "columnId"),
                    getLong(request, "rowId"),
                    getRating(request),
                    request.body(),
                    getUsername(request)
                    ));
    }

    private void registerAddRating(String path) {
        postForDatum(path, (request, response) ->
            scenarioRatingItemService.add(
                    getId(request),
                    getLong(request, "appId"),
                    getLong(request, "columnId"),
                    getLong(request, "rowId"),
                    getRating(request),
                    getUsername(request)
                    ));
    }

    private char getRating(Request request) {
        return request.params("rating").charAt(0);
    }

    private void registerRemoveRating(String path) {
        deleteForDatum(path, (request, response) ->
            scenarioRatingItemService.remove(
                    getId(request),
                    getLong(request, "appId"),
                    getLong(request, "columnId"),
                    getLong(request, "rowId")));
    }


    private void registerFindScenariosByRoadmapSelector(String path) {
        postForList(path, (request, response) ->
            scenarioService.findScenariosByRoadmapSelector(readIdSelectionOptionsFromBody(request)));
    }


    private void registerCloneScenario(String path) {
        postForDatum(path, (request, response) ->
                scenarioService.cloneScenario(ImmutableCloneScenarioCommand
                        .builder()
                        .newName(request.body())
                        .scenarioId(getId(request))
                        .userId(getUsername(request))
                        .build()));
    }



    private void registerGetScenarioById(String path) {
        getForDatum(path, (req, resp) -> {
            long scenarioId = getId(req);

            Scenario scenario = scenarioService.getById(scenarioId);
            return ImmutableFullScenario
                    .builder()
                    .scenario(scenario)
                    .axisDefinitions(scenarioAxisItemService.findForScenarioId(scenarioId))
                    .ratings(scenarioRatingItemService.findForScenarioId(scenarioId))
                    .roadmap(roadmapService.getById(scenario.roadmapId()))
                    .build();
        });
    }


    private void registerFindScenariosForRoadmapId(String path) {
        getForList(path, (req, resp) ->
                scenarioService.findForRoadmapId(getLong(req, "roadmapId")));
    }

}
