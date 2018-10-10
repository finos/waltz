package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.roadmap.RoadmapAndScenarioOverview;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.service.roadmap.RoadmapUtilities.mkBasicLogEntry;

@Service
public class RoadmapService {

    private final RoadmapDao roadmapDao;
    private final ScenarioDao scenarioDao;
    private final RoadmapIdSelectorFactory roadmapIdSelectorFactory;
    private final ChangeLogService changeLogService;


    @Autowired
    public RoadmapService(RoadmapDao roadmapDao,
                          ScenarioDao scenarioDao,
                          RoadmapIdSelectorFactory roadmapIdSelectorFactory,
                          ChangeLogService changeLogService) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        checkNotNull(roadmapIdSelectorFactory, "roadmapIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.roadmapDao = roadmapDao;
        this.scenarioDao = scenarioDao;
        this.roadmapIdSelectorFactory = roadmapIdSelectorFactory;
        this.changeLogService = changeLogService;
    }


    public Roadmap getById(long id) {
        return roadmapDao.getById(id);
    }


    public Collection<Roadmap> findRoadmapsBySelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = roadmapIdSelectorFactory.apply(selectionOptions);
        return roadmapDao.findRoadmapsBySelector(selector);
    }


    public Boolean updateDescription(long id, String newDescription, String userId) {
        Boolean result = roadmapDao.updateDescription(id, newDescription, userId);
        if (result) {
            writeLogEntriesForUpdate(id, "Updated Description", newDescription, userId);
        }
        return result;
    }


    public Boolean updateName(long id, String newName, String userId) {
        Boolean result = roadmapDao.updateName(id, newName, userId);
        if (result) {
            writeLogEntriesForUpdate(id, "Updated Name", newName, userId);
        }
        return result;
    }


    public Scenario addScenario(long roadmapId, String name, String userId) {
        changeLogService.write(ImmutableChangeLog
                .copyOf(mkBasicLogEntry(roadmapId, String.format("Added scenario %s", name), userId))
                .withOperation(Operation.ADD));
        return scenarioDao.add(roadmapId, name, userId);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByRatedEntity(EntityReference ratedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByRatedEntity(ratedEntity);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByFormalRelationship(EntityReference relatedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByFormalRelationship(relatedEntity);
    }


    // -- helpers --

    private void writeLogEntriesForUpdate(long roadmapId, String desc, String newValue, String userId) {
        String message = String.format("%s: '%s'", desc, newValue);
        changeLogService.write(mkBasicLogEntry(roadmapId, message, userId));
    }

}
