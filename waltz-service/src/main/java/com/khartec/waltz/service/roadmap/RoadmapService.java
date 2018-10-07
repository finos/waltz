package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.roadmap.RoadmapAndScenarioOverview;
import com.khartec.waltz.model.scenario.Scenario;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class RoadmapService {

    private final RoadmapDao roadmapDao;
    private final ScenarioDao scenarioDao;
    private RoadmapIdSelectorFactory roadmapIdSelectorFactory;


    @Autowired
    public RoadmapService(RoadmapDao roadmapDao, ScenarioDao scenarioDao, RoadmapIdSelectorFactory roadmapIdSelectorFactory) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        checkNotNull(roadmapIdSelectorFactory, "roadmapIdSelectorFactory cannot be null");
        this.roadmapDao = roadmapDao;
        this.scenarioDao = scenarioDao;
        this.roadmapIdSelectorFactory = roadmapIdSelectorFactory;
    }


    public Roadmap getById(long id) {
        return roadmapDao.getById(id);
    }


    public Collection<Roadmap> findRoadmapsRelatedToReference(EntityReference ref) {
        throw new UnsupportedOperationException("TODO"); //TODO: implement
    }


    public Collection<Roadmap> findRoadmapsByAxisReference(EntityReference ref) {
        throw new UnsupportedOperationException("TODO"); //TODO: implement
    }


    public Collection<Roadmap> findRoadmapsBySelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = roadmapIdSelectorFactory.apply(selectionOptions);
        return roadmapDao.findRoadmapsBySelector(selector);
    }


    public Boolean updateDescription(long id, String newDescription, String userId) {
        return roadmapDao.updateDescription(id, newDescription, userId);
    }


    public Boolean updateName(long id, String newName, String userId) {
        return roadmapDao.updateName(id, newName, userId);
    }


    public Scenario addScenario(long roadmapId, String name, String userId) {
        return scenarioDao.add(roadmapId, name, userId);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByRatedEntity(EntityReference ratedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByRatedEntity(ratedEntity);
    }

    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByFormalRelationship(EntityReference relatedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByFormalRelationship(relatedEntity);
    }
}
