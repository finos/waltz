package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.scenario.ScenarioAxisItemService;
import com.khartec.waltz.service.scenario.ScenarioRatingItemService;
import com.khartec.waltz.service.scenario.ScenarioService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.ImmutableFullScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getLong;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

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
        registerFindScenariosForRoadmapId(mkPath("by-roadmap-id", ":roadmapId"));
        registerGetScenarioById(mkPath("id", ":id"));
    }


    private void registerGetScenarioById(String path) {
        getForDatum(path, (req, resp) -> {
            long roadmapId = getLong(req, "roadmapId");
            long scenarioId = getLong(req, "scenarioId");

            return ImmutableFullScenario
                    .builder()
                    .roadmap(roadmapService.getById(roadmapId))
                    .scenario(scenarioService.getById(scenarioId))
                    .axisDefinitions(scenarioAxisItemService.findForScenarioId(scenarioId))
                    .ratings(scenarioRatingItemService.findForScenarioId(scenarioId))
                    .build();
        });
    }


    private void registerFindScenariosForRoadmapId(String path) {
        getForList(path, (req, resp) ->
                scenarioService.findForRoadmapId(getLong(req, "roadmapId")));
    }

}
