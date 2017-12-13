/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.entity_named_note.EntityNamedNodeType;
import com.khartec.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.entity_named_note.EntityNamedNoteTypeService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class EntityNamedNoteTypeEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "entity-named-note-type");


    private final EntityNamedNoteTypeService entityNamedNoteTypeService;
    private final UserRoleService userRoleService;


    @Autowired
    public EntityNamedNoteTypeEndpoint(EntityNamedNoteTypeService entityNamedNoteTypeService,
                                       UserRoleService userRoleService) {
        checkNotNull(entityNamedNoteTypeService, "entityNamedNoteTypeService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.entityNamedNoteTypeService = entityNamedNoteTypeService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findAllPath = BASE;
        String removePath = mkPath(BASE, ":id");
        String updatePath = mkPath(BASE, ":id");
        String createPath = mkPath(BASE);


        ListRoute<EntityNamedNodeType> findAllRoute = (req, res) ->
                entityNamedNoteTypeService.findAll();

        DatumRoute<Long> createRoute = (req, res) -> {
            ensureUserHasAdminRights(req);
            EntityNamedNoteTypeChangeCommand command = readBody(req, EntityNamedNoteTypeChangeCommand.class);
            return entityNamedNoteTypeService.create(command, getUsername(req));
        };

        DatumRoute<Boolean> updateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);
            EntityNamedNoteTypeChangeCommand command = readBody(req, EntityNamedNoteTypeChangeCommand.class);
            return entityNamedNoteTypeService.update(getId(req), command, getUsername(req));
        };
        DatumRoute<Boolean> removeRoute = (req, res) -> {
            ensureUserHasAdminRights(req);
            return entityNamedNoteTypeService.removeById(getId(req), getUsername(req));
        };


        getForList(findAllPath, findAllRoute);
        deleteForDatum(removePath, removeRoute);
        postForDatum(createPath, createRoute);
        putForDatum(updatePath, updateRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.ADMIN);
    }

}


