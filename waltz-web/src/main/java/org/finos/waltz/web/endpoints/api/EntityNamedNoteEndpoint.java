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

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.StringChangeCommand;
import org.finos.waltz.model.entity_named_note.EntityNamedNote;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.entity_named_note.EntityNamedNoteService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class EntityNamedNoteEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "entity-named-note");

    private final EntityNamedNoteService entityNamedNoteService;
    private final UserRoleService userRoleService;
    private final AppGroupService appGroupService;


    @Autowired
    public EntityNamedNoteEndpoint(EntityNamedNoteService entityNamedNoteService,
                                   AppGroupService appGroupService,
                                   UserRoleService userRoleService) {
        checkNotNull(entityNamedNoteService, "entityNamedNoteService cannot be null");
        checkNotNull(appGroupService, "appGroupService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.entityNamedNoteService = entityNamedNoteService;
        this.appGroupService = appGroupService;
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
            ensureHasPermission(ref, req, Operation.REMOVE);
            return entityNamedNoteService.remove(
                    ref,
                    getLong(req,"noteTypeId"),
                    getUsername(req));
        };

        DatumRoute<Boolean> saveRoute = (req, res) -> {
            EntityReference ref = getEntityReference(req);
            ensureHasPermission(ref, req, Operation.ADD);
            StringChangeCommand command = WebUtilities.readBody(req, StringChangeCommand.class);
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


    private void ensureHasPermission(EntityReference ref, Request req, Operation op) throws InsufficientPrivelegeException {
        switch (ref.kind()) {
            case APP_GROUP:
                appGroupService.verifyUserCanUpdateGroup(getUsername(req), ref.id());
                break;
            default:
                WebUtilities.requireEditRoleForEntity(
                        userRoleService,
                        req,
                        ref.kind(),
                        op,
                        EntityKind.ENTITY_NAMED_NOTE);
        }
    }

}
