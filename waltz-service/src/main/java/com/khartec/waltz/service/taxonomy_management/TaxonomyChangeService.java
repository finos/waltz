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

package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.taxonomy_management.TaxonomyChangeDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.exceptions.NotAuthorizedException;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.client_cache_key.ClientCacheKeyService;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_category.MeasurableCategoryService;
import com.khartec.waltz.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.*;
import static java.util.stream.Collectors.toMap;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class TaxonomyChangeService {

    private final TaxonomyChangeDao taxonomyChangeDao;
    private final Map<TaxonomyChangeType, TaxonomyCommandProcessor> processorsByType;
    private final ClientCacheKeyService clientCacheKeyService;
    private final UserRoleService userRoleService;
    private final MeasurableCategoryService measurableCategoryService;
    private final MeasurableService measurableService;
    private final EntityHierarchyService entityHierarchyService;


    @Autowired
    public TaxonomyChangeService(TaxonomyChangeDao taxonomyChangeDao,
                                 ClientCacheKeyService clientCacheKeyService,
                                 MeasurableCategoryService measurableCategoryService,
                                 UserRoleService userRoleService,
                                 List<TaxonomyCommandProcessor> processors,
                                 MeasurableService measurableService,
                                 EntityHierarchyService entityHierarchyService) {
        checkNotNull(taxonomyChangeDao, "taxonomyChangeDao cannot be null");
        checkNotNull(clientCacheKeyService, "clientCacheKeyService cannot be null");
        this.clientCacheKeyService = clientCacheKeyService;
        this.taxonomyChangeDao = taxonomyChangeDao;
        this.userRoleService = userRoleService;
        this.measurableCategoryService = measurableCategoryService;
        processorsByType = processors
                .stream()
                .flatMap(p -> p.supportedTypes()
                        .stream()
                        .map(st -> tuple(st, p)))
                .collect(toMap(t -> t.v1, t -> t.v2));
        this.measurableService = measurableService;
        this.entityHierarchyService = entityHierarchyService;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand command) {
        TaxonomyCommandProcessor processor = getCommandProcessor(command);
        return processor.preview(command);
    }


    public TaxonomyChangePreview previewById(long id) {
        TaxonomyChangeCommand command = taxonomyChangeDao.getDraftCommandById(id);
        return preview(command);
    }


    public TaxonomyChangeCommand submitDraftChange(TaxonomyChangeCommand draftCommand, String userId) {
        verifyUserHasPermissions(userId, draftCommand.changeDomain());
        checkTrue(draftCommand.status() == TaxonomyChangeLifecycleStatus.DRAFT, "Command must be DRAFT");

        TaxonomyChangeCommand commandToSave = ImmutableTaxonomyChangeCommand
                .copyOf(draftCommand)
                .withCreatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withCreatedBy(userId);

        return taxonomyChangeDao
                .createCommand(commandToSave);
    }


    public Collection<TaxonomyChangeCommand> findDraftChangesByDomain(EntityReference domain) {
        return taxonomyChangeDao
                .findChangesByDomainAndStatus(domain, TaxonomyChangeLifecycleStatus.DRAFT);
    }


    public TaxonomyChangeCommand applyById(long id, String userId) {
        TaxonomyChangeCommand command = taxonomyChangeDao.getDraftCommandById(id);
        verifyUserHasPermissions(userId, command.changeDomain());

        checkFalse(isMoveToSameParent(command),
                "Measurable cannot set it self as its parent.");
        checkFalse(isMoveToANodeWhichIsAlreadyAChild(command),
                "Parent node is already a child of the measurable.");

        TaxonomyCommandProcessor processor = getCommandProcessor(command);
        TaxonomyChangeCommand updatedCommand = processor.apply(command, userId);
        clientCacheKeyService.createOrUpdate("TAXONOMY");

        updatedCommand = taxonomyChangeDao.update(updatedCommand);

        // rebuild measurable hierarchy
        if (command.changeDomain().kind() == EntityKind.MEASURABLE_CATEGORY
                && isHierarchyChange(command)) {
            entityHierarchyService.buildForMeasurableByCategory(command.changeDomain().id());
        }

        return updatedCommand;
    }


    public boolean removeById(long id, String userId) {
        verifyUserHasPermissions(userId);
        return taxonomyChangeDao.removeById(id, userId);
    }


    private TaxonomyCommandProcessor getCommandProcessor(TaxonomyChangeCommand command) {
        TaxonomyCommandProcessor processor = processorsByType.get(command.changeType());
        checkNotNull(processor, "Cannot find processor for type: %s", command.changeType());
        return processor;
    }


    private void verifyUserHasPermissions(String userId, EntityReference changeDomain) {
        verifyUserHasPermissions(userId);

        if (changeDomain.kind() == EntityKind.MEASURABLE_CATEGORY) {
            MeasurableCategory category = measurableCategoryService.getById(changeDomain.id());
            if (! category.editable()) {
                throw new NotAuthorizedException("Unauthorised: Category is not editable");
            }
        }
    }

    private void verifyUserHasPermissions(String userId) {
        if (! userRoleService.hasRole(userId, SystemRole.TAXONOMY_EDITOR.name())) {
            throw new NotAuthorizedException();
        }
    }

    private boolean isMoveToSameParent(TaxonomyChangeCommand command) {
        String destinationId = command.params().get("destinationId");
        if(isMovingToANode(command, destinationId)) {

            long parentId = Long.parseLong(destinationId);
            final Measurable parent = measurableService.getById(parentId);

            return parent.parentId().isPresent()
                    && command.primaryReference().id() == parent.parentId().get();
        }
        return false;
    }

    private boolean isMoveToANodeWhichIsAlreadyAChild(TaxonomyChangeCommand command) {
        String destinationId = command.params().get("destinationId");
        return isMovingToANode(command, destinationId) &&
             Long.parseLong(destinationId) == command.primaryReference().id();
    }

    private boolean isMovingToANode(TaxonomyChangeCommand command, String destinationId) {
        return command.changeType().equals(TaxonomyChangeType.MOVE) &&
                StringUtilities.notEmpty(destinationId);
    }


    private boolean isHierarchyChange(TaxonomyChangeCommand command) {
        return command.changeType() == TaxonomyChangeType.ADD_CHILD
                || command.changeType() == TaxonomyChangeType.ADD_PEER
                || command.changeType() == TaxonomyChangeType.REMOVE
                || command.changeType() == TaxonomyChangeType.MOVE;
    }
}
