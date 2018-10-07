package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.scenario.ImmutableCloneScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioStatus;
import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.scenario.ScenarioAxisItemService;
import com.khartec.waltz.service.scenario.ScenarioRatingItemService;
import com.khartec.waltz.service.scenario.ScenarioService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.ImmutableFullScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.time.LocalDate;

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
        registerUpdateName(mkPath(BASE_URL, "id", ":id", "name"));
        registerUpdateDescription(mkPath(BASE_URL, "id", ":id", "description"));
        registerUpdateEffectiveDate(mkPath(BASE_URL, "id", ":id", "effective-date"));
        registerUpdateScenarioStatus(mkPath(BASE_URL, "id", ":id", "scenario-status", ":scenarioStatus"));

        registerLoadAxis(mkPath(BASE_URL, "id", ":id", "axis", ":orientation"));
        registerReorderAxis(mkPath(BASE_URL, "id", ":id", "axis", ":orientation", "reorder"));
        registerAddAxisItem(mkPath(BASE_URL, "id", ":id", "axis", ":orientation", ":domainItemKind", ":domainItemId"));
        registerRemoveAxisItem(mkPath(BASE_URL, "id", ":id", "axis", ":orientation", ":domainItemKind", ":domainItemId"));

    }


    private void registerReorderAxis(String path) {
        postForDatum(path, (request, response) ->
                scenarioService.reorderAxis(
                        getId(request),
                        getOrientation(request),
                        readIdsFromBody(request)));
    }


    private void registerLoadAxis(String path) {
        getForList(path, (request, response) ->
                scenarioService.loadAxis(
                        getId(request),
                        getOrientation(request)));
    }


    private void registerAddAxisItem(String path) {
        postForDatum(path, (request, response) ->
                scenarioService.addAxisItem(
                        getId(request),
                        getOrientation(request),
                        getDomainItem(request),
                        readBody(request, Integer.class),
                        getUsername(request)));
    }


    private void registerRemoveAxisItem(String path) {
        deleteForDatum(path, (request, response) ->
                scenarioService.removeAxisItem(
                        getId(request),
                        getOrientation(request),
                        getDomainItem(request),
                        getUsername(request)));
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


    private void registerUpdateEffectiveDate(String path) {
        postForDatum(path, (req, resp) ->
                scenarioService.updateEffectiveDate(
                        getId(req),
                        readBody(req, LocalDate.class),
                        getUsername(req)));
    }


    private void registerUpdateScenarioStatus(String path) {
        postForDatum(path, (req, resp) ->
                scenarioService.updateScenarioStatus(
                        getId(req),
                        readEnum(req, "scenarioStatus", ScenarioStatus.class, (s) -> ScenarioStatus.CURRENT),
                        getUsername(req)));
    }


    private void registerUpdateName(String path) {
        postForDatum(path, (req, resp) ->
                scenarioService.updateName(
                        getId(req),
                        req.body(),
                        getUsername(req)));
    }


    private void registerUpdateDescription(String path) {
        postForDatum(path, (req, resp) ->
                scenarioService.updateDescription(
                        getId(req),
                        req.body(),
                        getUsername(req)));
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


    // -- helpers --

    private char getRating(Request request) {
        return request.params("rating").charAt(0);
    }


    private AxisOrientation getOrientation(Request request) {
        return readEnum(
                request,
                "orientation",
                AxisOrientation.class,
                s -> AxisOrientation.ROW);
    }


    private EntityReference getDomainItem(Request request) {
        return getEntityReference(
                request,
                "domainItemKind",
                "domainItemId");
    }

}
