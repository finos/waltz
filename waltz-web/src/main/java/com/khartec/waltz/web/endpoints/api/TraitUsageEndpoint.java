/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import com.khartec.waltz.model.trait.Trait;
import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.trait.TraitService;
import com.khartec.waltz.service.trait.TraitUsageService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class TraitUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "trait-usage");
    private static final Logger LOG = getLogger(TraitUsageEndpoint.class);

    private final TraitUsageService traitUsageService;
    private final TraitService traitService;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public TraitUsageEndpoint(TraitUsageService traitUsageService,
                              TraitService traitService,
                              ChangeLogService changeLogService,
                              UserRoleService userRoleService) {
        this.traitUsageService = traitUsageService;
        this.traitService = traitService;
        this.changeLogService = changeLogService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String findByEntityKindPath = mkPath(BASE_URL, "entity", ":kind");
        String findByEntityReferencePath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByTraitIdPath = mkPath(BASE_URL, "trait", ":id");
        String addTraitUsagePath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String removeTraitUsagePath = mkPath(BASE_URL, "entity", ":kind", ":id", ":traitId");


        ListRoute<TraitUsage> findAllRoute = (request, response) -> traitUsageService.findAll();
        ListRoute<TraitUsage> findByEntityKindRoute = (request, response) -> traitUsageService.findByEntityKind(getKind(request));
        ListRoute<TraitUsage> findByEntityReferenceRoute = (request, response) -> traitUsageService.findByEntityReference(getEntityReference(request));
        ListRoute<TraitUsage> findByTraitIdRoute = (request, response) -> traitUsageService.findByTraitId(getId(request));

        ListRoute<TraitUsage> addTraitUsageRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.APP_EDITOR);
            long traitId = readBody(request, Long.class);
            EntityReference entityReference = getEntityReference(request);

            Trait trait = traitService.getById(traitId);

            if (trait == null) {
                LOG.warn("Could not find trait to add");
                return traitUsageService.findByEntityReference(entityReference);
            }

            List<TraitUsage> usages = traitUsageService.addTraitUsage(entityReference, traitId);

            changeLogService.write(ImmutableChangeLog.builder()
                    .severity(Severity.INFORMATION)
                    .parentReference(entityReference)
                    .userId(getUsername(request))
                    .message("Added trait: " + trait.name())
                    .operation(Operation.UPDATE)
                    .build());

            return usages;
        };

        ListRoute<TraitUsage> removeTraitUsageRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.APP_EDITOR);
            long traitId = getLong(request, "traitId");
            EntityReference entityReference = getEntityReference(request);

            Trait trait = traitService.getById(traitId);

            if (trait == null) {
                LOG.warn("Could not find trait to remove, proceeding anyway");
                // we may be cleaning up incorrect historical data
            }

            List<TraitUsage> usages = traitUsageService.removeTraitUsage(entityReference, traitId);

            changeLogService.write(ImmutableChangeLog.builder()
                    .severity(Severity.INFORMATION)
                    .parentReference(entityReference)
                    .userId(getUsername(request))
                    .message("Removed trait: "+ (trait == null ? traitId : trait.name()))
                    .operation(Operation.UPDATE)
                    .build());

            return usages;
        };


        getForList(findAllPath, findAllRoute);
        getForList(findByEntityKindPath, findByEntityKindRoute);
        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        getForList(findByTraitIdPath, findByTraitIdRoute);

        postForList(addTraitUsagePath, addTraitUsageRoute);
        deleteForList(removeTraitUsagePath, removeTraitUsageRoute);

    }
}
