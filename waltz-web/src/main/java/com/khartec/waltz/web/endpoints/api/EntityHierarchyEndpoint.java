package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.user.Role.ADMIN;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class EntityHierarchyEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntityHierarchyEndpoint.class);
    private static final String BASE = mkPath("api", "entity-hierarchy");

    private final EntityHierarchyService entityHierarchyService;
    private final UserRoleService userRoleService;


    @Autowired
    public EntityHierarchyEndpoint(EntityHierarchyService entityHierarchyService, UserRoleService userRoleService) {
        checkNotNull(entityHierarchyService, "entityHierarchyService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.entityHierarchyService = entityHierarchyService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findTalliesPath = mkPath(BASE, "tallies");
        String findRootTalliesPath = mkPath(BASE, "root-tallies");
        String findRootsPath = mkPath(BASE, "roots", ":kind");
        String buildByKindPath = mkPath(BASE, "build", ":kind");

        ListRoute<StringTally> findTalliesRoute = (request, response) -> entityHierarchyService.tallyByKind();
        ListRoute<StringTally> findRootTalliesRoute = (request, response) -> entityHierarchyService.getRootTallies();
        ListRoute<EntityReference> findRootsRoute = (request, response) -> entityHierarchyService.getRoots(getKind(request));

        getForList(findTalliesPath, findTalliesRoute);
        getForList(findRootTalliesPath, findRootTalliesRoute);
        getForList(findRootsPath, findRootsRoute);
        postForDatum(buildByKindPath, this::buildByKindRoute);
    }


    private int buildByKindRoute(Request request, Response response) {
        requireRole(userRoleService, request, ADMIN);
        EntityKind kind = getKind(request);
        LOG.info("Building entity hierarchy for kind: {}", kind);
        return entityHierarchyService.buildFor(kind);
    }

}
