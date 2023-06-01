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

package org.finos.waltz.service.measurable;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.data.measurable.search.MeasurableSearchDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.measurable.Measurable;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.isEmpty;
import static org.finos.waltz.model.EntityReference.mkRef;


@Service
public class MeasurableService {

    private final MeasurableDao measurableDao;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    private final MeasurableSearchDao measurableSearchDao;
    private final ChangeLogService changeLogService;
    private final EntityReferenceNameResolver nameResolver;


    @Autowired
    public MeasurableService(MeasurableDao measurableDao,
                             MeasurableSearchDao measurableSearchDao,
                             EntityReferenceNameResolver nameResolver,
                             ChangeLogService changeLogService) {
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableSearchDao, "measurableSearchDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.measurableDao = measurableDao;
        this.measurableSearchDao = measurableSearchDao;
        this.nameResolver = nameResolver;
        this.changeLogService = changeLogService;
    }


    public List<Measurable> findAll() {
        return measurableDao.findAll();
    }


    public List<Measurable> findByMeasurableIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableDao.findByMeasurableIdSelector(selector);
    }


    public List<Measurable> findByCategoryId(Long categoryId) {
        return measurableDao.findByCategoryId(categoryId);
    }


    public Map<String, Long> findExternalIdToIdMapByCategoryId(Long categoryId) {
        checkNotNull(categoryId, "categoryId cannot be null");

        return measurableDao.findExternalIdToIdMapByCategoryId(categoryId);
    }


    public Collection<Measurable> search(String query) {
        if (StringUtilities.isEmpty(query)) {
            return Collections.emptyList();
        }
        return search(EntitySearchOptions.mkForEntity(EntityKind.MEASURABLE, query));
    }


    public Collection<Measurable> search(EntitySearchOptions options) {
        return measurableSearchDao.search(options);
    }


    public Collection<Measurable> findByExternalId(String extId) {
        return measurableDao.findByExternalId(extId);
    }


    public Measurable getById(long id) {
        return measurableDao.getById(id);
    }


    public Collection<Measurable> findByOrgUnitId(Long id) {
        return measurableDao.findByOrgUnitId(id);
    }


    public boolean updateConcreteFlag(Long id, boolean newValue, String userId) {
        logUpdate(id, "concrete flag", Boolean.toString(newValue), m -> Optional.of(Boolean.toString(m.concrete())), userId);

        return measurableDao.updateConcreteFlag(id, newValue, userId);
    }


    public boolean updateName(long id, String newValue, String userId) {
        logUpdate(id, "name", newValue, m -> ofNullable(m.name()), userId);
        return measurableDao.updateName(id, newValue, userId);
    }


    public boolean updateDescription(long id, String newValue, String userId) {
        logUpdate(id, "description", newValue, m -> ofNullable(m.description()), userId);
        return measurableDao.updateDescription(id, newValue, userId);
    }


    public boolean updateExternalId(long id, String newValue, String userId) {
        logUpdate(id, "externalId", newValue, ExternalIdProvider::externalId, userId);
        return measurableDao.updateExternalId(id, newValue, userId);
    }


    public boolean create(Measurable measurable, String userId) {
        Long measurableId = measurableDao.create(measurable);
        writeAuditMessage(measurableId, userId, String.format("created new measurable %s", measurable.name()));
        return measurableId > 1;
    }


    public int deleteByIdSelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = measurableIdSelectorFactory
                .apply(selectionOptions);
        return measurableDao
                .deleteByIdSelector(selector);
    }


    /**
     * Changes the parentId of the given measurable to the new parent specified
     * by destinationId.  If destination id is null the measurable will be a new
     * root node in the tree.
     *
     * @param measurableId  measurable id of item to move
     * @param destinationId new parent id (or null if root)
     * @param userId        who initiated this move
     */
    public boolean updateParentId(Long measurableId, Long destinationId, String userId) {
        checkNotNull(measurableId, "Cannot updateParentId a measurable with a null id");

        writeAuditMessage(
                measurableId,
                userId,
                format("Measurable: [%s] moved to new parent: [%s]",
                        resolveName(measurableId),
                        destinationId == null
                                ? "<root of tree>"
                                : resolveName(destinationId)));

        return measurableDao.updateParentId(measurableId, destinationId, userId);
    }


    public boolean reorder(long categoryId,
                           List<Long> ids,
                           String userId) {
        if (isEmpty(ids)) {
            return true;
        }

        measurableDao.reorder(
                categoryId,
                ids,
                userId);

        changeLogService.write(ImmutableChangeLog.builder()
                .severity(Severity.INFORMATION)
                .userId(userId)
                .operation(Operation.UPDATE)
                .parentReference(mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId))
                .createdAt(DateTimeUtilities.nowUtc())
                .message(format("Reordered %d measurables", ids.size()))
                .build());

        return true;
    }

    // --- helpers ---

    private void logUpdate(long id, String valueName, String newValue, Function<Measurable, Optional<String>> valueExtractor, String userId) {
        String existingValue = ofNullable(measurableDao.getById(id))
                .flatMap(valueExtractor)
                .orElse("<null>");

        writeAuditMessage(
                id,
                userId,
                format("Measurable: [%s] %s updated, from: [%s] to: [%s]",
                        resolveName(id),
                        valueName,
                        existingValue,
                        newValue));
    }


    private String resolveName(long id) {
        return nameResolver
                .resolve(mkRef(EntityKind.MEASURABLE, id))
                .flatMap(EntityReference::name)
                .orElse("UNKNOWN");
    }


    public void writeAuditMessage(Long measurableId, String userId, String msg) {
        changeLogService.write(ImmutableChangeLog.builder()
                .severity(Severity.INFORMATION)
                .userId(userId)
                .operation(Operation.UPDATE)
                .parentReference(mkRef(EntityKind.MEASURABLE, measurableId))
                .createdAt(DateTimeUtilities.nowUtc())
                .message(msg)
                .build());
    }

    public boolean moveChildren(Long measurableId, Long targetMeasurableId, String userId) {
        return measurableDao.moveChildren(measurableId, targetMeasurableId, userId);
    }
}