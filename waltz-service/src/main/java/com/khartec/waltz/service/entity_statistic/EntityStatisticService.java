package com.khartec.waltz.service.entity_statistic;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityStatisticService {

    private final ApplicationIdSelectorFactory factory;
    private final EntityStatisticDao entityStatisticDao;


    @Autowired
    public EntityStatisticService(ApplicationIdSelectorFactory factory,
                                  EntityStatisticDao entityStatisticDao) {
        checkNotNull(factory, "factory cannot be null");
        checkNotNull(entityStatisticDao, "entityStatisticDao cannot be null");

        this.factory = factory;
        this.entityStatisticDao = entityStatisticDao;
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        return entityStatisticDao.findStatisticsForEntity(ref, active);
    }

}
