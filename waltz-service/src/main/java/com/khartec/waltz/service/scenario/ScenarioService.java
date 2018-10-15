package com.khartec.waltz.service.scenario;

import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.data.scenario.ScenarioAxisItemDao;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.data.scenario.ScenarioRatingItemDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.scenario.CloneScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioType;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.roadmap.RoadmapUtilities;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.service.scenario.ScenarioUtilities.mkBasicLogEntry;

@Service
public class ScenarioService {

    private final ScenarioDao scenarioDao;
    private final ScenarioAxisItemDao scenarioAxisItemDao;
    private final ScenarioRatingItemDao scenarioRatingItemDao;
    private final RoadmapIdSelectorFactory roadmapIdSelectorFactory;
    private final ChangeLogService changeLogService;


    @Autowired
    public ScenarioService(ScenarioDao scenarioDao,
                           ScenarioAxisItemDao scenarioAxisItemDao,
                           ScenarioRatingItemDao scenarioRatingItemDao,
                           RoadmapIdSelectorFactory roadmapIdSelectorFactory, ChangeLogService changeLogService) {
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        checkNotNull(scenarioAxisItemDao, "scenarioAxisItemDao cannot be null");
        checkNotNull(scenarioRatingItemDao, "scenarioRatingItemDao cannot be null");
        checkNotNull(roadmapIdSelectorFactory, "roadmapIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.scenarioDao = scenarioDao;
        this.scenarioAxisItemDao = scenarioAxisItemDao;
        this.scenarioRatingItemDao = scenarioRatingItemDao;
        this.roadmapIdSelectorFactory = roadmapIdSelectorFactory;
        this.changeLogService = changeLogService;
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

        writeLogEntriesForCloningOperation(command, clonedScenario);

        return clonedScenario;
    }


    public Boolean updateName(long scenarioId, String newValue, String userId) {
        writeLogEntriesForUpdate(scenarioId, "Updated Name", newValue, userId);
        return scenarioDao.updateName(scenarioId, newValue, userId);
    }


    public Boolean updateDescription(long scenarioId, String newValue, String userId) {
        writeLogEntriesForUpdate(scenarioId, "Updated Description", newValue, userId);
        return scenarioDao.updateDescription(scenarioId, newValue, userId);
    }


    public Boolean updateEffectiveDate(long scenarioId, LocalDate newValue, String userId) {
        writeLogEntriesForUpdate(scenarioId, "Updated Effective Date", newValue.toString(), userId);
        return scenarioDao.updateEffectiveDate(scenarioId, newValue, userId);
    }


    public Boolean updateScenarioType(long scenarioId, ScenarioType newType, String userId) {
        writeLogEntriesForUpdate(scenarioId, "Updated Scenario Type", newType.name(), userId);
        return scenarioDao.updateScenarioType(
                scenarioId,
                newType,
                userId);
    }


    public Boolean updateReleaseStatus(long scenarioId, ReleaseLifecycleStatus newStatus, String userId) {
        writeLogEntriesForUpdate(scenarioId, "Updated Release  Status", newStatus.name(), userId);
        return scenarioDao.updateReleaseStatus(
                scenarioId,
                newStatus,
                userId);
    }


    public Boolean updateEntityLifecycleStatus(long scenarioId, EntityLifecycleStatus newStatus, String userId) {
        writeLogEntriesForUpdate(scenarioId, "Updated Lifecycle Status", newStatus.name(), userId);
        return scenarioDao.updateEntityLifecycleStatus(
                scenarioId,
                newStatus,
                userId);
    }


    public Boolean removeScenario(long scenarioId, String userId) {
        Scenario scenario = scenarioDao.getById(scenarioId);
        changeLogService.write(ImmutableChangeLog
                .copyOf(RoadmapUtilities.mkBasicLogEntry(
                        scenario.roadmapId(),
                        String.format("Removed scenario: %s", scenario.name()),
                        userId))
                .withOperation(Operation.REMOVE));
        return scenarioDao.removeScenario(scenarioId, userId);
    }


    // -- helpers --

    private void writeLogEntriesForUpdate(long scenarioId, String desc, String newValue, String userId) {
        String message = String.format("%s: '%s'", desc, newValue);
        changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));
    }


    private void writeLogEntriesForCloningOperation(CloneScenarioCommand command, Scenario clonedScenario) {
        String message = String.format("Created cloned scenario: '%s'", command.newName());
        ChangeLog logEntry = mkBasicLogEntry(command.scenarioId(), message, command.userId());

        changeLogService.write(logEntry);
        changeLogService.write(ImmutableChangeLog
                .copyOf(logEntry)
                .withParentReference(EntityReference.mkRef(EntityKind.ROADMAP, clonedScenario.roadmapId())));
    }

}
