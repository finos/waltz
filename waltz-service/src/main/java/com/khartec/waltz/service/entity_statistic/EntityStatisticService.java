package com.khartec.waltz.service.entity_statistic;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDefinitionDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticSummaryDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.EntityStatisticValue;
import com.khartec.waltz.model.immediate_hierarchy.ImmediateHierarchy;
import com.khartec.waltz.model.immediate_hierarchy.ImmediateHierarchyUtilities;
import com.khartec.waltz.model.tally.TallyPack;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

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


    public List<EntityStatisticDefinition> findTopLevelDefinitions() {
        return definitionDao.findTopLevelDefinitions();
    }


    public ImmediateHierarchy<EntityStatisticDefinition> findRelatedStatDefinitions(long id) {
        List<EntityStatisticDefinition> defs = definitionDao.findRelated(id);
        ImmediateHierarchy<EntityStatisticDefinition> relations = ImmediateHierarchyUtilities.build(id, defs);
        return relations;
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        return statisticDao.findStatisticsForEntity(ref, active);
    }


    public List<EntityStatisticValue> getStatisticValuesForAppIdSelector(long statisticId, ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);

        return valueDao.getStatisticValuesForAppIdSelector(statisticId, appIdSelector);
    }


    public List<TallyPack<String>> findStatTallies(List<Long> statisticIds, ApplicationIdSelectionOptions options) {
        Checks.checkNotNull(statisticIds, "statisticIds cannot be null");
        Checks.checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = factory.apply(options);
        return summaryDao.findStatTallies(statisticIds, appIdSelector);
    }


    public EntityStatisticDefinition findDefinition(long id) {
        return definitionDao.getDefinition(id);
    }


    public List<EntityStatisticDefinition> findAllActiveDefinitions() {
        return definitionDao.findAllActiveDefinitions();
    }
}
