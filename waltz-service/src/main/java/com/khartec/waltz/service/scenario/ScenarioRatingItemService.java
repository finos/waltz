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

    public boolean remove(long scenarioId, long appId, long columnId, long rowId) {
        return scenarioRatingItemDao.remove(scenarioId, appId, columnId, rowId);
    }

    public boolean add(long scenarioId, long appId, long columnId, long rowId, char rating, String userId) {
        return scenarioRatingItemDao.add(scenarioId, appId, columnId, rowId, rating, userId);
    }

    public boolean saveComment(long scenarioId, long appId, long columnId, long rowId, String comment, String userId) {
        return scenarioRatingItemDao.saveComment(scenarioId, appId, columnId, rowId, comment, userId);
    }
}
