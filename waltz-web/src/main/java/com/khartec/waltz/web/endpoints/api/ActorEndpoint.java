package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.actor.ActorChangeCommand;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.actor.ActorCreateCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.actor.ActorService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class ActorEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ActorEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "actor");

    private final ActorService service;
    private UserRoleService userRoleService;


    @Autowired
    public ActorEndpoint(ActorService service, UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // read
        getForList(BASE_URL, (request, response) -> service.findAll());
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute );

        // create
        postForDatum(mkPath(BASE_URL, "update"), this::createRoute);

        // update
        putForDatum(mkPath(BASE_URL, "update"), this::updateRoute);

        // delete
        deleteForDatum(mkPath(BASE_URL, ":id"), this::deleteRoute);

    }


    private Actor getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }


    private Long createRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        ActorCreateCommand command = readBody(request, ActorCreateCommand.class);
        String username = getUsername(request);
        LOG.info("User: {} creating Involvement Kind: {}", username, command);

        return service.create(command, username);
    }


    private CommandResponse<ActorChangeCommand> updateRoute(Request request, Response response)
            throws IOException {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);
        ActorChangeCommand command = readBody(request, ActorChangeCommand.class);

        LOG.info("User: {} updating Involvement Kind: {}", username, command);
        return service.update(command, username);
    }


    private boolean deleteRoute(Request request, Response response) {
        ensureUserHasAdminRights(request);

        long id = getId(request);
        String username = getUsername(request);

        LOG.info("User: {} removing Involvement Kind: {}", username, id);

        return service.delete(id);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.ADMIN);
    }

}
