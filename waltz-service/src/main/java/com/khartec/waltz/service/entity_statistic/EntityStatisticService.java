package com.khartec.waltz.service.entity_statistic;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDefinitionDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticSummaryDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.entity_statistic.EntityStatisticSummary;
import com.khartec.waltz.model.entity_statistic.EntityStatisticValue;
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


    public List<EntityStatisticSummary> findStatsSummariesForAppIdSelector(ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);

        return summaryDao.findForAppIdSelector(appIdSelector);
    }


    public List<EntityStatisticSummary> findRelatedStatsSummaries(long id, ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);

        return summaryDao.findRelated(id, appIdSelector);
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        return statisticDao.findStatisticsForEntity(ref, active);
    }


    public List<EntityStatisticValue> getStatisticValuesForAppIdSelector(long statisticId, ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);

        return valueDao.getStatisticValuesForAppIdSelector(statisticId, appIdSelector);
    }

}
