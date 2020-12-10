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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.StringChangeCommand;
import com.khartec.waltz.model.entity_named_note.EntityNamedNote;
import com.khartec.waltz.service.entity_named_note.EntityNamedNoteService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.deleteForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.putForDatum;

@Service
public class EntityNamedNoteEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "entity-named-note");

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
        String findByEntityReferencePath = mkPath(BASE, "entity-ref", ":kind", ":id");
        String savePath = mkPath(BASE, "entity-ref", ":kind", ":id", ":noteTypeId");
        String removePath = mkPath(BASE, "entity-ref", ":kind", ":id", ":noteTypeId");

        ListRoute<EntityNamedNote> findByEntityReferenceRoute = (req, res)
                -> entityNamedNoteService.findByEntityReference(getEntityReference(req));

        DatumRoute<Boolean> removeRoute = (req, res) -> {
            EntityReference ref = getEntityReference(req);
            ensureHasPermission(ref.kind(), req, Operation.REMOVE);
            return entityNamedNoteService.remove(
                    ref,
                    getLong(req,"noteTypeId"),
                    getUsername(req));
        };

        DatumRoute<Boolean> saveRoute = (req, res) -> {
            EntityReference ref = getEntityReference(req);
            ensureHasPermission(ref.kind(), req, Operation.ADD);
            StringChangeCommand command = readBody(req, StringChangeCommand.class);
            return entityNamedNoteService.save(
                    ref,
                    getLong(req,"noteTypeId"),
                    command.newStringVal().orElse(null),
                    getUsername(req));
        };

        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        putForDatum(savePath, saveRoute);
        deleteForDatum(removePath, removeRoute);
    }


    private void ensureHasPermission(EntityKind kind, Request req, Operation op) {
        requireEditRoleForEntity(userRoleService, req, kind, op, EntityKind.ENTITY_NAMED_NOTE);
    }

}
