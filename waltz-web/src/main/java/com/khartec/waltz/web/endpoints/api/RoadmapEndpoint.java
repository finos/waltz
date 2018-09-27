package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

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
        registerGetRoadmapById(mkPath(BASE_URL, "id", ":roadmapId"));
    }



    private void registerFindRoadmapsBySelector(String path) {
        postForList(path, (req, resp) ->
                roadmapService.findRoadmapsBySelector(readIdSelectionOptionsFromBody(req)));
    }


    private void registerGetRoadmapById(String path) {
        getForDatum(path, (req, resp) ->
                roadmapService.getById(getLong(req, "roadmapId")));
    }
}
