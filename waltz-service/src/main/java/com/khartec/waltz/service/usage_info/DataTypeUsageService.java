/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.service.usage_info;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

@Service
public class DataTypeUsageService {

    private final DataTypeUsageDao dataTypeUsageDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactor = new ApplicationIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();


    @Autowired
    public DataTypeUsageService(DataTypeUsageDao dataTypeUsageDao) {
        checkNotNull(dataTypeUsageDao, "dataTypeUsageDao cannot be null");
        this.dataTypeUsageDao = dataTypeUsageDao;
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
            List<UsageInfo> usages) {

        Collection<UsageInfo> base = CollectionUtilities.map(
                dataTypeUsageDao.findForEntityAndDataType(entityReference, dataTypeId),
                DataTypeUsage::usage);

        SystemChangeSet<UsageInfo, UsageKind> changeSet = mkChangeSet(
                SetUtilities.fromCollection(base),
                SetUtilities.fromCollection(usages));

        dataTypeUsageDao.insertUsageInfo(entityReference, dataTypeId, changeSet.inserts());
        dataTypeUsageDao.deleteUsageInfo(entityReference, dataTypeId, changeSet.deletes());
        dataTypeUsageDao.updateUsageInfo(entityReference, dataTypeId, changeSet.updates());

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

}
