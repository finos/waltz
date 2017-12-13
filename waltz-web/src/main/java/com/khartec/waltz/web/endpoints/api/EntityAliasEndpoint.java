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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.entity_alias.EntityAliasService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class EntityAliasEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntityAliasEndpoint.class);
    private static final String BASE_URL = mkPath("api", "entity", "alias");


    private final EntityAliasService entityAliasService;
    private final ChangeLogService changeLogService;


    @Autowired
    public EntityAliasEndpoint(EntityAliasService entityAliasService, ChangeLogService changeLogService) {
        checkNotNull(entityAliasService, "entityAliasService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.entityAliasService = entityAliasService;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {
        String updatePath = mkPath(BASE_URL, ":kind", ":id");
        String getPath = mkPath(BASE_URL, ":kind", ":id");

        ListRoute<String> getRoute = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            return entityAliasService.findAliasesForEntityReference(ref);
        };

        ListRoute<String> updateRoute = (req, resp) -> {
            String user = getUsername(req);
            // TODO: ensure user has role...

            EntityReference ref = getEntityReference(req);

            List<String> aliases = readStringsFromBody(req);
            String auditMessage = String.format(
                    "Updated alias, entity: %s, new aliases: %s, user: %s",
                    ref,
                    aliases,
                    user);

            entityAliasService.updateAliases(ref, aliases);

            LOG.info(auditMessage);
            changeLogService.write(ImmutableChangeLog.builder()
                    .parentReference(ref)
                    .userId(user)
                    .message(auditMessage)
                    .severity(Severity.INFORMATION)
                    .operation(Operation.UPDATE)
                    .build());

            return aliases;
        };

        postForList(updatePath, updateRoute);
        getForList(getPath, getRoute);
    }
}
