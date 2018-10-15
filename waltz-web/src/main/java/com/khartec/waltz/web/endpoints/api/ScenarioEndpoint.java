package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.scenario.ImmutableCloneScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioType;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.scenario.ScenarioAxisItemService;
import com.khartec.waltz.service.scenario.ScenarioRatingItemService;
import com.khartec.waltz.service.scenario.ScenarioService;
import com.khartec.waltz.service.user.UserRoleService;
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
    private final UserRoleService userRoleService;


    @Autowired
    public ScenarioEndpoint(RoadmapService roadmapService,
                            ScenarioService scenarioService,
                            ScenarioAxisItemService scenarioAxisItemService,
                            ScenarioRatingItemService scenarioRatingItemService,
                            UserRoleService userRoleService) {
        checkNotNull(roadmapService, "roadmapService cannot be null");
        checkNotNull(scenarioService, "scenarioService cannot be null");
        checkNotNull(scenarioAxisItemService, "scenarioAxisItemService cannot be null");
        checkNotNull(scenarioRatingItemService, "scenarioRatingItemService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.roadmapService = roadmapService;
        this.scenarioService = scenarioService;
        this.scenarioAxisItemService = scenarioAxisItemService;
        this.scenarioRatingItemService = scenarioRatingItemService;
        this.userRoleService = userRoleService;
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
        registerUpdateScenarioType(mkPath(BASE_URL, "id", ":id", "scenario-type", ":scenarioType"));
        registerUpdateReleaseStatus(mkPath(BASE_URL, "id", ":id", "release-status", ":releaseStatus"));
        registerUpdateEntityLifecycleStatus(mkPath(BASE_URL, "id", ":id", "entity-lifecycle-status", ":lifecycleStatus"));

        registerLoadAxis(mkPath(BASE_URL, "id", ":id", "axis", ":orientation"));
        registerReorderAxis(mkPath(BASE_URL, "id", ":id", "axis", ":orientation", "reorder"));
        registerAddAxisItem(mkPath(BASE_URL, "id", ":id", "axis", ":orientation", ":domainItemKind", ":domainItemId"));
        registerRemoveAxisItem(mkPath(BASE_URL, "id", ":id", "axis", ":orientation", ":domainItemKind", ":domainItemId"));
        registerRemoveScenario(mkPath(BASE_URL, "id", ":id"));
    }


    private void registerRemoveScenario(String path) {
        deleteForDatum(path, (request, response) -> {
            ensureUserHasAdminRights(request);
            return scenarioService.removeScenario(
                    getId(request),
                    getUsername(request));
        });
    }


    private void registerReorderAxis(String path) {
        postForDatum(path, (request, response) -> {
                ensureUserHasEditRights(request);
                return scenarioAxisItemService.reorderAxis(
                        getId(request),
                        getOrientation(request),
                        readIdsFromBody(request),
                        getUsername(request));
        });
    }


    private void registerLoadAxis(String path) {
        getForList(path, (request, response) ->
                scenarioAxisItemService.loadAxis(
                        getId(request),
                        getOrientation(request)));
    }


    private void registerAddAxisItem(String path) {
        postForDatum(path, (request, response) -> {
            ensureUserHasEditRights(request);
            return scenarioAxisItemService.addAxisItem(
                    getId(request),
                    getOrientation(request),
                    getDomainItem(request),
                    readBody(request, Integer.class),
                    getUsername(request));
        });
    }


    private void registerRemoveAxisItem(String path) {
        deleteForDatum(path, (request, response) -> {
            ensureUserHasEditRights(request);
            return scenarioAxisItemService.removeAxisItem(
                    getId(request),
                    getOrientation(request),
                    getDomainItem(request),
                    getUsername(request));
        });
    }


    private void registerUpdateRating(String path) {
        postForDatum(path, (request, response) ->
        {
            ensureUserHasEditRights(request);
            return scenarioRatingItemService.updateRating(
                    getId(request),
                    getLong(request, "appId"),
                    getLong(request, "columnId"),
                    getLong(request, "rowId"),
                    getRating(request),
                    request.body(),
                    getUsername(request));
        });
    }


    private void registerUpdateEffectiveDate(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasEditRights(request);
            return scenarioService.updateEffectiveDate(
                    getId(request),
                    readBody(request, LocalDate.class),
                    getUsername(request));
        });
    }


    private void registerUpdateScenarioType(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasEditRights(request);
            return scenarioService.updateScenarioType(
                    getId(request),
                    readEnum(request, "scenarioType", ScenarioType.class, (s) -> ScenarioType.CURRENT),
                    getUsername(request));
        });
    }


    private void registerUpdateReleaseStatus(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasEditRights(request);
            return scenarioService.updateReleaseStatus(
                    getId(request),
                    readEnum(request, "releaseStatus", ReleaseLifecycleStatus.class, (s) -> ReleaseLifecycleStatus.ACTIVE),
                    getUsername(request));
        });
    }


    private void registerUpdateEntityLifecycleStatus(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasAdminRights(request);
            return scenarioService.updateEntityLifecycleStatus(
                    getId(request),
                    readEnum(request, "lifecycleStatus", EntityLifecycleStatus.class, (s) -> EntityLifecycleStatus.PENDING),
                    getUsername(request));
        });
    }


    private void registerUpdateName(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasEditRights(request);
            return scenarioService.updateName(
                    getId(request),
                    request.body(),
                    getUsername(request));
        });
    }


    private void registerUpdateDescription(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasEditRights(request);
            return scenarioService.updateDescription(
                    getId(request),
                    request.body(),
                    getUsername(request));
        });
    }


    private void registerAddRating(String path) {
        postForDatum(path, (request, response) -> {
            ensureUserHasEditRights(request);
            return scenarioRatingItemService.add(
                    getId(request),
                    getLong(request, "appId"),
                    getLong(request, "columnId"),
                    getLong(request, "rowId"),
                    getRating(request),
                    getUsername(request));
        });
    }


    private void registerRemoveRating(String path) {
        deleteForDatum(path, (request, response) -> {
            ensureUserHasEditRights(request);
            return scenarioRatingItemService.remove(
                    getId(request),
                    getLong(request, "appId"),
                    getLong(request, "columnId"),
                    getLong(request, "rowId"),
                    getUsername(request));
        });
    }


    private void registerFindScenariosByRoadmapSelector(String path) {
        postForList(path, (request, response) ->
            scenarioService.findScenariosByRoadmapSelector(readIdSelectionOptionsFromBody(request)));
    }


    private void registerCloneScenario(String path) {
        postForDatum(path, (request, response) -> {
            ensureUserHasAdminRights(request);
            return scenarioService.cloneScenario(ImmutableCloneScenarioCommand
                    .builder()
                    .newName(request.body())
                    .scenarioId(getId(request))
                    .userId(getUsername(request))
                    .build());
        });
    }


    private void registerGetScenarioById(String path) {
        getForDatum(path, (request, resp) -> {
            long scenarioId = getId(request);

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
        getForList(path, (request, resp) ->
                scenarioService.findForRoadmapId(getLong(request, "roadmapId")));
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


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.SCENARIO_ADMIN);
    }


    private void ensureUserHasEditRights(Request request) {
        requireAnyRole(userRoleService, request, Role.SCENARIO_EDITOR, Role.SCENARIO_ADMIN);
    }
}
