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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.service.entity_named_note.EntityNamedNoteService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.StringChangeCommand;
import org.finos.waltz.model.entity_named_note.EntityNamedNote;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntityNamedNoteEndpoint implements Endpoint {

    private static final String BASE = WebUtilities.mkPath("api", "entity-named-note");

    private final EntityNamedNoteService entityNamedNoteService;
    private final UserRoleService userRoleService;


    @Autowired
    public EntityNamedNoteEndpoint(EntityNamedNoteService entityNamedNoteService,
                                   UserRoleService userRoleService) {
        checkNotNull(entityNamedNoteService, "entityNamedNoteService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.entityNamedNoteService = entityNamedNoteService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByEntityReferencePath = WebUtilities.mkPath(BASE, "entity-ref", ":kind", ":id");
        String savePath = WebUtilities.mkPath(BASE, "entity-ref", ":kind", ":id", ":noteTypeId");
        String removePath = WebUtilities.mkPath(BASE, "entity-ref", ":kind", ":id", ":noteTypeId");

        ListRoute<EntityNamedNote> findByEntityReferenceRoute = (req, res)
                -> entityNamedNoteService.findByEntityReference(WebUtilities.getEntityReference(req));

        DatumRoute<Boolean> removeRoute = (req, res) -> {
            EntityReference ref = WebUtilities.getEntityReference(req);
            ensureHasPermission(ref.kind(), req, Operation.REMOVE);
            return entityNamedNoteService.remove(
                    ref,
                    WebUtilities.getLong(req,"noteTypeId"),
                    WebUtilities.getUsername(req));
        };

        DatumRoute<Boolean> saveRoute = (req, res) -> {
            EntityReference ref = WebUtilities.getEntityReference(req);
            ensureHasPermission(ref.kind(), req, Operation.ADD);
            StringChangeCommand command = WebUtilities.readBody(req, StringChangeCommand.class);
            return entityNamedNoteService.save(
                    ref,
                    WebUtilities.getLong(req,"noteTypeId"),
                    command.newStringVal().orElse(null),
                    WebUtilities.getUsername(req));
        };

        EndpointUtilities.getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        EndpointUtilities.putForDatum(savePath, saveRoute);
        EndpointUtilities.deleteForDatum(removePath, removeRoute);
    }


    private void ensureHasPermission(EntityKind kind, Request req, Operation op) {
        WebUtilities.requireEditRoleForEntity(userRoleService, req, kind, op, EntityKind.ENTITY_NAMED_NOTE);
    }

}
