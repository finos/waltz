package com.khartec.waltz.service.scenario;

import com.khartec.waltz.data.scenario.ScenarioRatingItemDao;
import com.khartec.waltz.model.scenario.ScenarioRatingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ScenarioRatingItemService {

    private final ScenarioRatingItemDao scenarioRatingItemDao;


    @Autowired
    public ScenarioRatingItemService(ScenarioRatingItemDao scenarioRatingItemDao) {
        checkNotNull(scenarioRatingItemDao, "scenarioRatingItemDao cannot be null");
        this.scenarioRatingItemDao = scenarioRatingItemDao;
    }


    public Collection<ScenarioRatingItem> findForScenarioId(long scenarioId) {
        return scenarioRatingItemDao.findForScenarioId(scenarioId);
    }

}
