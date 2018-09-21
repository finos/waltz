package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.roadmap.ScenarioRatingItemDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.roadmap.ImmutableScenarioRatingItem;
import com.khartec.waltz.model.roadmap.ScenarioRatingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.service.roadmap.ScenarioAxisItemService.*;
import static com.khartec.waltz.service.roadmap.ScenarioService.exampleScenario;

@Service
public class ScenarioRatingItemService {

    static ScenarioRatingItem rating1 = ImmutableScenarioRatingItem
            .builder()
            .item(mkRef(EntityKind.APPLICATION, 361L))
            .column(col1.item())
            .row(row1.item())
            .rating('G')
            .scenarioId(exampleScenario.id().get())
            .lastUpdatedBy("admin")
            .build();


    static ScenarioRatingItem rating2 = ImmutableScenarioRatingItem
            .copyOf(rating1)
            .withColumn(col2.item());


    static ScenarioRatingItem rating3 = ImmutableScenarioRatingItem
            .copyOf(rating1)
            .withItem(mkRef(EntityKind.APPLICATION, 388L))
            .withRow(row2.item())
            .withRating('R')

            .withColumn(col1.item());

    static ScenarioRatingItem rating4a = ImmutableScenarioRatingItem
            .copyOf(rating1)
            .withItem(mkRef(EntityKind.APPLICATION, 394L))
            .withRating('A')
            .withRow(row2.item())
            .withColumn(col2.item());


    static ScenarioRatingItem rating4b = ImmutableScenarioRatingItem
            .copyOf(rating4a)
            .withItem(mkRef(EntityKind.APPLICATION, 388L))
            .withRating('G')
            .withRow(row2.item())
            .withColumn(col2.item());


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
