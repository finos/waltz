package com.khartec.waltz.jobs;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowStatsDao;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.involvement.InvolvementService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.common.FunctionUtilities.time;

/**
 * Created by dwatkins on 13/05/2016.
 */
public class ApplicationIdSelectorHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ApplicationIdSelectorFactory factory = ctx.getBean(ApplicationIdSelectorFactory.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        DataFlowService service = ctx.getBean(DataFlowService.class);
        DataFlowStatsDao dao = ctx.getBean(DataFlowStatsDao.class);
        InvolvementService involvementService = ctx.getBean(InvolvementService.class);
        InvolvementDao involvementDao = ctx.getBean(InvolvementDao.class);

        ApplicationIdSelectionOptions options = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference
                        .builder()
                        .kind(EntityKind.PERSON)
                        .id(3455)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();


        Select<Record1<Long>> selector = factory.apply(options);


        time("selector", () -> dsl
                .fetch(selector)
                .size());

        time("tally", () -> dao
                .tallyDataTypes(selector)
                .size());

        time("appCount", () -> dao
                .countDistinctAppInvolvement(selector));

        time("flowCount", () -> dao
                .countDistinctFlowInvolvement(selector));

        time("service", () -> service
                .calculateStats(options));
    }


}
