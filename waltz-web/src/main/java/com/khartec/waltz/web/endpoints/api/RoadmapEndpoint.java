package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.roadmap.ScenarioAxisItemService;
import com.khartec.waltz.service.roadmap.ScenarioRatingItemService;
import com.khartec.waltz.service.roadmap.ScenarioService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.FullScenario;
import com.khartec.waltz.web.json.ImmutableFullScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.getLong;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class RoadmapEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "roadmap");
    private final RoadmapService roadmapService;
    private final ScenarioService scenarioService;
    private final ScenarioAxisItemService scenarioAxisItemService;
    private final ScenarioRatingItemService scenarioRatingItemService;


    @Autowired
    public RoadmapEndpoint(RoadmapService roadmapService,
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

        String getRoadmapByIdPath = mkPath(BASE_URL, "by-id", ":roadmapId");
        String findScenariosForRoadmapPath = mkPath(getRoadmapByIdPath, "scenario");
        String getScenarioByIdPath = mkPath(getRoadmapByIdPath, "scenario", ":scenarioId");

        getForDatum(getRoadmapByIdPath, (req, resp) ->
                roadmapService.getById(getLong(req, "roadmapId")));

        getForList(findScenariosForRoadmapPath, (req, resp) ->
                scenarioService.findForRoadmapId(getLong(req, "roadmapId")));

        getForDatum(getScenarioByIdPath, (req, resp) -> {
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
}
