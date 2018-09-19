package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.roadmap.ImmutableScenario;
import com.khartec.waltz.model.roadmap.Scenario;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ScenarioService {

    static Scenario s1 = ImmutableScenario
            .builder()
            .id(1L)
            .name("2020 Hard Brexit")
            .lastUpdatedBy("admin")
            .status(ReleaseLifecycleStatus.ACTIVE)
            .roadmapId(RoadmapService.r1.id().get())
            .build();

    public Scenario getById(long id) {
        return s1;
    }

    public Collection<Scenario> findForRoadmapId(long roadmapId) {
        return ListUtilities.newArrayList(s1);
    }

}
