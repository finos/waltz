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

    private final ApplicationIdSelectorFactory factory;
    private final EntityStatisticValueDao valueDao;
    private final EntityStatisticDefinitionDao definitionDao;
    private final EntityStatisticSummaryDao summaryDao;
    private final EntityStatisticDao statisticDao;


    @Autowired
    public EntityStatisticService(ApplicationIdSelectorFactory factory,
                                  EntityStatisticValueDao valueDao,
                                  EntityStatisticDefinitionDao definitionDao,
                                  EntityStatisticSummaryDao summaryDao,
                                  EntityStatisticDao statisticDao
                                  ) {
        checkNotNull(factory, "factory cannot be null");
        checkNotNull(valueDao, "valueDao cannot be null");
        checkNotNull(definitionDao, "definitionDao cannot be null");
        checkNotNull(summaryDao, "summaryDao cannot be null");
        checkNotNull(statisticDao, "statisticDao cannot be null");

        this.factory = factory;
        this.valueDao = valueDao;
        this.definitionDao = definitionDao;
        this.summaryDao = summaryDao;
        this.statisticDao = statisticDao;

    }


    public ImmediateHierarchy<EntityStatisticDefinition> getRelatedStatDefinitions(long id, boolean rollupOnly) {
        List<EntityStatisticDefinition> defs = definitionDao.findRelated(id, rollupOnly);
        ImmediateHierarchy<EntityStatisticDefinition> relations = ImmediateHierarchyUtilities.build(id, defs);
        return relations;
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
                d -> d.rollupKind(),
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
