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

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.entity_alias.EntityAliasService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;
import static org.finos.waltz.common.Checks.checkNotNull;

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
                    "Aliases have been updated to: %s",
                    aliases);

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
