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

package com.khartec.waltz.service.entity_statistic;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDefinitionDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticSummaryDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.Duration;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.EntityStatisticValue;
import com.khartec.waltz.model.entity_statistic.RollupKind;
import com.khartec.waltz.model.immediate_hierarchy.ImmediateHierarchy;
import com.khartec.waltz.model.immediate_hierarchy.ImmediateHierarchyUtilities;
import com.khartec.waltz.model.tally.TallyPack;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.concat;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static java.util.Collections.emptyList;

@Service
public class EntityStatisticService {

    private final ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();
    private final EntityStatisticValueDao valueDao;
    private final EntityStatisticDefinitionDao definitionDao;
    private final EntityStatisticSummaryDao summaryDao;
    private final EntityStatisticDao statisticDao;


    @Autowired
    public EntityStatisticService(EntityStatisticValueDao valueDao,
                                  EntityStatisticDefinitionDao definitionDao,
                                  EntityStatisticSummaryDao summaryDao,
                                  EntityStatisticDao statisticDao)
    {
        checkNotNull(valueDao, "valueDao cannot be null");
        checkNotNull(definitionDao, "definitionDao cannot be null");
        checkNotNull(summaryDao, "summaryDao cannot be null");
        checkNotNull(statisticDao, "statisticDao cannot be null");

        this.valueDao = valueDao;
        this.definitionDao = definitionDao;
        this.summaryDao = summaryDao;
        this.statisticDao = statisticDao;

    }


    public ImmediateHierarchy<EntityStatisticDefinition> getRelatedStatDefinitions(long id, boolean rollupOnly) {
        List<EntityStatisticDefinition> defs = definitionDao.findRelated(id, rollupOnly);
        return ImmediateHierarchyUtilities.build(id, defs);
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        return statisticDao.findStatisticsForEntity(ref, active);
    }


    public List<EntityStatisticValue> getStatisticValuesForAppIdSelector(long statisticId, IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);

        return valueDao.getStatisticValuesForAppIdSelector(statisticId, appIdSelector);
    }


    public List<Application> getStatisticAppsForAppIdSelector(long statisticId, IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);

        return valueDao.getStatisticAppsForAppIdSelector(statisticId, appIdSelector);
    }


    public List<TallyPack<String>> findStatTallies(List<Long> statisticIds, IdSelectionOptions options) {
        Checks.checkNotNull(statisticIds, "statisticIds cannot be null");
        Checks.checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        Map<RollupKind, Collection<Long>> definitionIdsByRollupKind = groupBy(
                EntityStatisticDefinition::rollupKind,
                d -> d.id().orElse(null),
                definitionDao.findByIds(statisticIds));


        return concat(
                summaryDao.generateWithCountByEntity(
                        definitionIdsByRollupKind.getOrDefault(RollupKind.COUNT_BY_ENTITY, emptyList()),
                        appIdSelector),
                summaryDao.generateWithSumByValue(
                        definitionIdsByRollupKind.getOrDefault(RollupKind.SUM_BY_VALUE, emptyList()),
                        appIdSelector),
                summaryDao.generateWithAvgByValue(
                        definitionIdsByRollupKind.getOrDefault(RollupKind.AVG_BY_VALUE, emptyList()),
                        appIdSelector),
                summaryDao.generateWithNoRollup(
                        definitionIdsByRollupKind.getOrDefault(RollupKind.NONE, emptyList()),
                        options.entityReference())
        );

    }


    public TallyPack<String> calculateStatTally(Long statisticId, RollupKind rollupKind, IdSelectionOptions options) {
        Checks.checkNotNull(statisticId, "statisticId cannot be null");
        Checks.checkNotNull(options, "options cannot be null");
        Checks.checkNotNull(rollupKind, "rollupKind cannot be null");

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        switch(rollupKind) {
            case COUNT_BY_ENTITY:
                return summaryDao.generateWithCountByEntity(statisticId, appIdSelector);
            case SUM_BY_VALUE:
                return summaryDao.generateWithSumByValue(statisticId, appIdSelector);
            case AVG_BY_VALUE:
                return summaryDao.generateWithAvgByValue(statisticId, appIdSelector);
            case NONE:
                return summaryDao.generateWithNoRollup(statisticId, options.entityReference());
            default:
                throw new UnsupportedOperationException(String.format("Rollup kind [%s] not supported.", rollupKind));
        }
    }


    public List<TallyPack<String>> calculateHistoricStatTally(Long statisticId,
                                                              RollupKind rollupKind,
                                                              IdSelectionOptions options,
                                                              Duration duration) {
        Checks.checkNotNull(statisticId, "statisticId cannot be null");
        Checks.checkNotNull(rollupKind, "rollupKind cannot be null");
        Checks.checkNotNull(options, "options cannot be null");
        Checks.checkNotNull(duration, "duration cannot be null");

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        switch(rollupKind) {
            case COUNT_BY_ENTITY:
                return summaryDao.generateHistoricWithCountByEntity(statisticId, appIdSelector, duration);
            case SUM_BY_VALUE:
                return summaryDao.generateHistoricWithSumByValue(statisticId, appIdSelector, duration);
            case AVG_BY_VALUE:
                return summaryDao.generateHistoricWithAvgByValue(statisticId, appIdSelector, duration);
            case NONE:
                return summaryDao.generateHistoricWithNoRollup(statisticId, options.entityReference(), duration);
            default:
                throw new UnsupportedOperationException(String.format("Rollup kind [%s] not supported.", rollupKind));
        }
    }


    public EntityStatisticDefinition getDefinitionById(long id) {
        return definitionDao.getById(id);
    }


    public List<EntityStatisticDefinition> findAllActiveDefinitions(boolean rollupOnly) {
        return definitionDao.findAllActiveDefinitions(rollupOnly);
    }
}
