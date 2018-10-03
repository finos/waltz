package com.khartec.waltz.service.scenario;

import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.data.scenario.ScenarioAxisItemDao;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.data.scenario.ScenarioRatingItemDao;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.scenario.CloneScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioAxisItem;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ScenarioService {

    private final ScenarioDao scenarioDao;
    private final ScenarioAxisItemDao scenarioAxisItemDao;
    private final ScenarioRatingItemDao scenarioRatingItemDao;
    private final RoadmapIdSelectorFactory roadmapIdSelectorFactory;


    @Autowired
    public ScenarioService(ScenarioDao scenarioDao,
                           ScenarioAxisItemDao scenarioAxisItemDao,
                           ScenarioRatingItemDao scenarioRatingItemDao,
                           RoadmapIdSelectorFactory roadmapIdSelectorFactory) {
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        checkNotNull(scenarioAxisItemDao, "scenarioAxisItemDao cannot be null");
        checkNotNull(scenarioRatingItemDao, "scenarioRatingItemDao cannot be null");
        checkNotNull(roadmapIdSelectorFactory, "roadmapIdSelectorFactory cannot be null");
        this.scenarioDao = scenarioDao;
        this.scenarioAxisItemDao = scenarioAxisItemDao;
        this.scenarioRatingItemDao = scenarioRatingItemDao;
        this.roadmapIdSelectorFactory = roadmapIdSelectorFactory;
    }


    public Scenario getById(long id) {
        return scenarioDao.getById(id);
    }


    public Collection<Scenario> findForRoadmapId(long roadmapId) {
        return scenarioDao.findForRoadmapId(roadmapId);
    }


    public Collection<Scenario> findScenariosByRoadmapSelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = roadmapIdSelectorFactory.apply(selectionOptions);
        return scenarioDao.findByRoadmapSelector(selector);
    }


    public Scenario cloneScenario(CloneScenarioCommand command) {
        Scenario clonedScenario = scenarioDao.cloneScenario(command);
        scenarioRatingItemDao.cloneItems(command, clonedScenario.id().get());
        scenarioAxisItemDao.cloneItems(command, clonedScenario.id().get());
        return clonedScenario;
    }


    public Boolean updateName(long scenarioId, String newValue, String userId) {
        return scenarioDao.updateName(scenarioId, newValue, userId);
    }


    public Boolean updateDescription(long scenarioId, String newValue, String userId) {
        return scenarioDao.updateDescription(scenarioId, newValue, userId);
    }


    public Boolean updateEffectiveDate(long scenarioId, LocalDate newValue, String userId) {
        return scenarioDao.updateEffectiveDate(scenarioId, newValue, userId);
    }


    public Boolean addAxisItem(long scenarioId,
                              AxisOrientation orientation,
                              EntityReference domainItem,
                              Integer position,
                              String userId) {
        return scenarioAxisItemDao.add(
                scenarioId,
                orientation,
                domainItem,
                position);
    }


    public Boolean removeAxisItem(long scenarioId,
                              AxisOrientation orientation,
                              EntityReference domainItem,
                              String userId) {
        return scenarioAxisItemDao.remove(
                scenarioId,
                orientation,
                domainItem);
    }

    public Collection<ScenarioAxisItem> loadAxis(long scenarioId, AxisOrientation orientation) {
        return scenarioAxisItemDao.findForScenarioAndOrientation(
                scenarioId,
                orientation);
    }
}
