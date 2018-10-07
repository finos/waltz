package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class RoadmapEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "roadmap");
    private final RoadmapService roadmapService;


    @Autowired
    public RoadmapEndpoint(RoadmapService roadmapService) {
        checkNotNull(roadmapService, "roadmapService cannot be null");

        this.roadmapService = roadmapService;
    }


    @Override
    public void register() {
        registerFindRoadmapsBySelector(mkPath(BASE_URL, "by-selector"));
        registerGetRoadmapById(mkPath(BASE_URL, "id", ":id"));
        registerUpdateName(mkPath(BASE_URL, "id", ":id", "name"));
        registerUpdateDescription(mkPath(BASE_URL, "id", ":id", "description"));
        registerAddScenario(mkPath(BASE_URL, "id", ":id", "add-scenario"));
        registerFindRoadmapsAndScenariosByRatedEntity(mkPath(BASE_URL, "by-rated-entity", ":kind", ":id"));
        registerFindRoadmapsAndScenariosByFormalRelationship(mkPath(BASE_URL, "by-formal-relationship", ":kind", ":id"));
    }


    private void registerFindRoadmapsAndScenariosByFormalRelationship(String path) {
        getForList(path, (request, response) ->
                roadmapService.findRoadmapsAndScenariosByFormalRelationship(
                        getEntityReference(request)));
    }


    private void registerFindRoadmapsAndScenariosByRatedEntity(String path) {
        getForList(path, (request, response) ->
                roadmapService.findRoadmapsAndScenariosByRatedEntity(
                        getEntityReference(request)));
    }


    private void registerAddScenario(String path) {
        postForDatum(path, (req, resp) ->
                roadmapService.addScenario(
                        getId(req),
                        req.body(),
                        getUsername(req)));
    }


    private void registerUpdateName(String path) {
        postForDatum(path, (req, resp) ->
                roadmapService.updateName(
                        getId(req),
                        req.body(),
                        getUsername(req)));
    }


    private void registerUpdateDescription(String path) {
        postForDatum(path, (req, resp) ->
                roadmapService.updateDescription(
                        getId(req),
                        req.body(),
                        getUsername(req)));
    }


    private void registerFindRoadmapsBySelector(String path) {
        postForList(path, (req, resp) ->
                roadmapService.findRoadmapsBySelector(readIdSelectionOptionsFromBody(req)));
    }


    private void registerGetRoadmapById(String path) {
        getForDatum(path, (req, resp) ->
                roadmapService.getById(getId(req)));
    }
}
