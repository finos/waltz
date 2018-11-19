package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.data.roadmap.RoadmapSearchDao;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.roadmap.RoadmapAndScenarioOverview;
import com.khartec.waltz.model.roadmap.RoadmapCreateCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.service.roadmap.RoadmapUtilities.mkBasicLogEntry;
import static java.util.stream.Collectors.toList;

@Service
public class RoadmapService {

    private final RoadmapDao roadmapDao;
    private final RoadmapSearchDao roadmapSearchDao;
    private final ScenarioDao scenarioDao;
    private final RoadmapIdSelectorFactory roadmapIdSelectorFactory;
    private final ChangeLogService changeLogService;
    private final EntityRelationshipDao entityRelationshipDao;


    @Autowired
    public RoadmapService(RoadmapDao roadmapDao,
                          RoadmapSearchDao roadmapSearchDao,
                          ScenarioDao scenarioDao,
                          RoadmapIdSelectorFactory roadmapIdSelectorFactory,
                          ChangeLogService changeLogService,
                          EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        checkNotNull(roadmapSearchDao, "roadmapSearchDao cannot be null");
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        checkNotNull(roadmapIdSelectorFactory, "roadmapIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.roadmapDao = roadmapDao;
        this.roadmapSearchDao = roadmapSearchDao;
        this.scenarioDao = scenarioDao;
        this.roadmapIdSelectorFactory = roadmapIdSelectorFactory;
        this.changeLogService = changeLogService;
        this.entityRelationshipDao = entityRelationshipDao;
    }


    public Roadmap getById(long id) {
        return roadmapDao.getById(id);
    }


    public Long createRoadmap(RoadmapCreateCommand command, String userId) {
        long roadmapId = roadmapDao.createRoadmap(
                command.name(),
                command.ratingSchemeId(),
                command.columnType(),
                command.rowType(),
                userId);

        if (roadmapId > 0) {
            changeLogService.write(ImmutableChangeLog
                    .copyOf(mkBasicLogEntry(roadmapId, String.format("Created roadmap: %s", command.name()), userId))
                    .withOperation(Operation.ADD));
        }

        EntityRelationship reln = ImmutableEntityRelationship.builder()
                .a(command.linkedEntity())
                .b(mkRef(EntityKind.ROADMAP, roadmapId))
                .relationship(RelationshipKind.RELATES_TO)
                .lastUpdatedBy(userId)
                .build();

        entityRelationshipDao.create(reln);

        return roadmapId;

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


    public Boolean updateLifecycleStatus(long id, EntityLifecycleStatus newStatus, String userId) {
        Boolean result = roadmapDao.updateLifecycleStatus(id, newStatus, userId);
        if (result) {
            writeLogEntriesForUpdate(id, "Updated Entity Lifecycle Status", newStatus.name(), userId);
        }
        return result;
    }


    public Scenario addScenario(long roadmapId, String name, String userId) {
        changeLogService.write(ImmutableChangeLog
                .copyOf(mkBasicLogEntry(roadmapId, String.format("Added scenario %s", name), userId))
                .withChildKind(EntityKind.SCENARIO)
                .withOperation(Operation.ADD));
        return scenarioDao.add(roadmapId, name, userId);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByRatedEntity(EntityReference ratedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByRatedEntity(ratedEntity);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByFormalRelationship(EntityReference relatedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByFormalRelationship(relatedEntity);
    }


    public List<EntityReference> search(String query) {
        List<Roadmap> roadmaps = search(query, EntitySearchOptions.mkForEntity(EntityKind.ROADMAP));
        return roadmaps.stream()
                .map(a -> a.entityReference())
                .collect(toList());
    }


    public List<Roadmap> search(String query, EntitySearchOptions options) {
        return roadmapSearchDao.search(query, options);
    }


    // -- helpers --

    private void writeLogEntriesForUpdate(long roadmapId, String desc, String newValue, String userId) {
        String message = String.format("%s: '%s'", desc, newValue);
        changeLogService.write(mkBasicLogEntry(roadmapId, message, userId));
    }

}
