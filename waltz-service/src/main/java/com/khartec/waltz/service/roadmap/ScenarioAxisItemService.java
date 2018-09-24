package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.roadmap.ScenarioAxisItemDao;
import com.khartec.waltz.model.roadmap.ScenarioAxisItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ScenarioAxisItemService {


    private final ScenarioAxisItemDao scenarioAxisItemDao;

    
    @Autowired
    public ScenarioAxisItemService(ScenarioAxisItemDao scenarioAxisItemDao) {
        checkNotNull(scenarioAxisItemDao, "scenarioAxisItemDao cannot be null");
        this.scenarioAxisItemDao = scenarioAxisItemDao;
    }

    public Collection<ScenarioAxisItem> findForScenarioId(long scenarioId) {
        return scenarioAxisItemDao.findForScenarioId(scenarioId);
    }



}
