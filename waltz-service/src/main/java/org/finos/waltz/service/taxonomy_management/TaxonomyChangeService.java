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

package org.finos.waltz.service.taxonomy_management;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.taxonomy_management.TaxonomyChangeDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.taxonomy_management.BulkTaxonomyItem;
import org.finos.waltz.model.taxonomy_management.BulkTaxonomyParseResult;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangeCommand;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangePreview;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeLifecycleStatus;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangePreview;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeType;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.client_cache_key.ClientCacheKeyService;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser.InputFormat;
import org.finos.waltz.service.user.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static org.finos.waltz.common.Checks.checkFalse;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StringUtilities.limit;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class TaxonomyChangeService {

    private static final Logger LOG = LoggerFactory.getLogger(TaxonomyChangeService.class);

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
        try {
            return preview(command);
        } catch (Exception e) {
            return ImmutableTaxonomyChangePreview
                    .builder()
                    .command(command)
                    .errorMessage(e.getMessage())
                    .build();
        }
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


    public Collection<TaxonomyChangeCommand> findAllChangesByDomain(EntityReference domain) {
        return taxonomyChangeDao
                .findChangesByDomain(domain);
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
            long categoryId = command.changeDomain().id();
            int insertCount = entityHierarchyService.buildForMeasurableByCategory(categoryId);
            LOG.info(
                    "Rebuilt measurable category: {},  inserted {} new records",
                    categoryId,
                    insertCount);
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


    public void bulkPreview(long categoryId,
                            String inputStr,
                            InputFormat format) {
        LOG.debug(
                "Bulk preview - category:{}, format:{}, inputStr:{}",
                categoryId,
                format,
                limit(inputStr, 40));

        MeasurableCategory category = measurableCategoryService.getById(categoryId);
        checkNotNull(category, "Unknown category: %d", categoryId);
        LOG.debug(
                "category: {} = {}({})",
                categoryId,
                category.name(),
                category.externalId());

        BulkTaxonomyParseResult result = new BulkTaxonomyItemParser().parse(inputStr, format);
        List<Measurable> existingMeasurables = measurableService.findByCategoryId(categoryId);
        Map<String, Measurable> existingByExtId = indexBy(existingMeasurables, m -> m.externalId().orElse(null));
        Map<String, BulkTaxonomyItem> givenByExtId = indexBy(result.parsedItems(), BulkTaxonomyItem::externalId);
        Set<String> allExtIds = union(existingByExtId.keySet(), givenByExtId.keySet());

    }
}
