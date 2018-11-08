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
