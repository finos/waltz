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

package org.finos.waltz.service.usage_info;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.data_type.DataTypeIdSelectorFactory;
import org.finos.waltz.data.data_type_usage.DataTypeUsageDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.data_type_usage.DataTypeUsage;
import org.finos.waltz.model.system.SystemChangeSet;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.model.usage_info.UsageInfo;
import org.finos.waltz.model.usage_info.UsageKind;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.tables.Actor.ACTOR;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.*;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;

@Service
public class DataTypeUsageService {

    private final DataTypeUsageDao dataTypeUsageDao;
    private final DataTypeDao dataTypeDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactor = new ApplicationIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private final ChangeLogService changeLogService;


    @Autowired
    public DataTypeUsageService(DataTypeUsageDao dataTypeUsageDao,
                                DataTypeDao dataTypeDao,
                                ChangeLogService changeLogService) {
        checkNotNull(dataTypeUsageDao, "dataTypeUsageDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.dataTypeUsageDao = dataTypeUsageDao;
        this.dataTypeDao = dataTypeDao;
        this.changeLogService = changeLogService;
    }


    public List<DataTypeUsage> findForAppIdSelector(EntityKind kind, IdSelectionOptions options) {
        return dataTypeUsageDao.findForIdSelector(
                kind,
                appIdSelectorFactor.apply(options));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        return dataTypeUsageDao.findForEntity(ref);
    }


    public List<DataTypeUsage> findForDataTypeSelector(IdSelectionOptions dataTypeOptions) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(dataTypeOptions);
        return dataTypeUsageDao.findForDataTypeSelector(selector);
    }


    public List<DataTypeUsage> findForEntityAndDataType(EntityReference entityReference, Long dataTypeId) {
        return dataTypeUsageDao.findForEntityAndDataType(entityReference, dataTypeId);
    }


    public List<Tally<String>> findUsageStatsForDataTypeSelector(IdSelectionOptions idSelectionOptions) {
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(idSelectionOptions);
        return dataTypeUsageDao.findUsageStatsForDataTypeSelector(dataTypeIdSelector, idSelectionOptions);
    }


    /**
     * Given a usage kind and a dt selector, this will return a map, keyed by datatype id,
     * of application references which exhibit that usage kind.
     *
     * (UsageKind, SelectionOptions) ->  { DataType.id -> [ EntityRef... ] }
     * @param usageKind
     * @param options
     * @return
     */
    public Map<Long, Collection<EntityReference>> findForUsageKindByDataTypeIdSelector(UsageKind usageKind,
                                                                                       IdSelectionOptions options) {
        checkNotNull(usageKind, "usageKind cannot be null");
        checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);
        return dataTypeUsageDao.findForUsageKindByDataTypeIdSelector(usageKind, dataTypeIdSelector, options);
    }


    public SystemChangeSet<UsageInfo, UsageKind> save(
            EntityReference entityReference,
            Long dataTypeId,
            List<UsageInfo> usages,
            String userId) {

        Collection<UsageInfo> base = map(
                dataTypeUsageDao.findForEntityAndDataType(entityReference, dataTypeId),
                DataTypeUsage::usage);

        SystemChangeSet<UsageInfo, UsageKind> changeSet = mkChangeSet(
                fromCollection(base),
                fromCollection(usages));

        dataTypeUsageDao.insertUsageInfo(entityReference, dataTypeId, changeSet.inserts());
        dataTypeUsageDao.deleteUsageInfo(entityReference, dataTypeId, changeSet.deletes());
        dataTypeUsageDao.updateUsageInfo(entityReference, dataTypeId, changeSet.updates());

        logChanges(userId, entityReference, dataTypeId, changeSet);

        return changeSet;
    }


    public boolean recalculateForAllApplications() {
        return dataTypeUsageDao.recalculateForAllApplications();
    }


    public boolean recalculateForApplications(Collection<EntityReference> refs) {
        checkNotNull(refs, "refs cannot be null");
        Set<Long> appIds = refs
                .stream()
                .filter(r -> r.kind() == EntityKind.APPLICATION)
                .map(EntityReference::id)
                .collect(Collectors.toSet());

        Set<Long> actorIds = refs
                .stream()
                .filter(r -> r.kind() == EntityKind.ACTOR)
                .map(EntityReference::id)
                .collect(Collectors.toSet());

        if (isEmpty(appIds) && isEmpty(actorIds)) {
            return true;
        } else {
            Select<Record1<Long>> appIdSelector = convertApplicationIdsToIdSelector(appIds);
            Select<Record1<Long>> actorIdSelector = convertActorIdsToIdSelector(actorIds);
            return dataTypeUsageDao.recalculateForIdSelector(EntityKind.APPLICATION, appIdSelector) &&
                    dataTypeUsageDao.recalculateForIdSelector(EntityKind.ACTOR, actorIdSelector);
        }
    }


    private Select<Record1<Long>> convertApplicationIdsToIdSelector(Set<Long> appIds) {
        return DSL.select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(appIds));
    }


    private Select<Record1<Long>> convertActorIdsToIdSelector(Set<Long> actorIds) {
        return DSL.select(ACTOR.ID)
                .from(ACTOR)
                .where(ACTOR.ID.in(actorIds));
    }


    private void logChanges(String user,
                            EntityReference ref,
                            Long dataTypeId,
                            SystemChangeSet<UsageInfo, UsageKind> changes) {
        String dataTypeName = dataTypeDao.getById(dataTypeId).name();

        maybe(changes.deletes(), deletes -> logDeletes(user, ref, dataTypeName, deletes));
        maybe(changes.updates(), updates -> logUpdates(user, ref, dataTypeName, updates));
        maybe(changes.inserts(), inserts -> logInserts(user, ref, dataTypeName, inserts));
    }


    private void logDeletes(String user,
                            EntityReference ref,
                            String dataTypeName,
                            Collection<UsageKind> deletes) {
        String message = "Removed usage kind/s: " + deletes + " for data type: " + dataTypeName;
        logChange(user, ref, message);
    }


    private void logInserts(String user,
                            EntityReference ref,
                            String dataTypeName,
                            Collection<UsageInfo> inserts) {
        Collection<UsageKind> kinds = map(inserts, UsageInfo::kind);
        String message = "Added usage kind/s: " + kinds + " for data type: " + dataTypeName;
        logChange(user, ref, message);
    }


    private void logUpdates(String user,
                            EntityReference ref,
                            String dataTypeName,
                            Collection<UsageInfo> updates) {
        Collection<UsageKind> kinds = map(updates, UsageInfo::kind);
        String message = "Updated usage kind/s: " + kinds + " for data type: " + dataTypeName;
        logChange(user, ref, message);
    }


    private void logChange(String userId,
                           EntityReference ref,
                           String message) {

        ChangeLog changeLogEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .childKind(EntityKind.LOGICAL_DATA_FLOW)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(changeLogEntry);
    }
}
