package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.roadmap.ImmutableScenarioAxisItem;
import com.khartec.waltz.model.roadmap.ScenarioAxisItem;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.roadmap.AxisKind.COLUMN;
import static com.khartec.waltz.model.roadmap.AxisKind.ROW;
import static com.khartec.waltz.service.roadmap.ScenarioService.exampleScenario;

@Service
public class ScenarioAxisItemService {

    static ScenarioAxisItem row1 = ImmutableScenarioAxisItem
            .builder()
            .id(10L)
            .axisKind(ROW)
            .item(mkRef(EntityKind.MEASURABLE, 652L))
            .order(10)
            .scenarioId(exampleScenario.id().get())
            .build();


    static ScenarioAxisItem row2 = ImmutableScenarioAxisItem
            .copyOf(row1)
            .withId(12L)
            .withItem(mkRef(EntityKind.MEASURABLE, 653L))
            .withOrder(20);


    static ScenarioAxisItem col1 = ImmutableScenarioAxisItem
            .copyOf(row1)
            .withId(13L)
            .withAxisKind(COLUMN)
            .withItem(mkRef(EntityKind.MEASURABLE, 614L));


    static ScenarioAxisItem col2 = ImmutableScenarioAxisItem
            .copyOf(col1)
            .withId(14L)
            .withItem(mkRef(EntityKind.MEASURABLE, 615L))
            .withOrder(20);


    public Collection<ScenarioAxisItem> findForScenarioId(long scenarioId) {
        return newArrayList(row1, row2, col1, col2);
    }



}
