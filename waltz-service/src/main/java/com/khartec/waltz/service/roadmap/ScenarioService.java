package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.roadmap.ScenarioDao;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.roadmap.ImmutableScenario;
import com.khartec.waltz.model.roadmap.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ScenarioService {

    static Scenario exampleScenario = ImmutableScenario
            .builder()
            .id(1L)
            .name("2020 Hard Brexit")
            .lastUpdatedBy("admin")
            .status(ReleaseLifecycleStatus.ACTIVE)
            .roadmapId(RoadmapService.r1.id().get())
            .build();

    private final ScenarioDao scenarioDao;


    @Autowired
    public ScenarioService(ScenarioDao scenarioDao) {
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        this.scenarioDao = scenarioDao;
    }


    public Scenario getById(long id) {
        return scenarioDao.getById(id);
    }

    public Collection<Scenario> findForRoadmapId(long roadmapId) {
        return ListUtilities.newArrayList(exampleScenario);
    }

}
