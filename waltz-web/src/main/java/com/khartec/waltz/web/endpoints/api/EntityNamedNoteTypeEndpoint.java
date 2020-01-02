/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.entity_named_note.EntityNamedNodeType;
import com.khartec.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import com.khartec.waltz.model.user.SystemRole;
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
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }

}


