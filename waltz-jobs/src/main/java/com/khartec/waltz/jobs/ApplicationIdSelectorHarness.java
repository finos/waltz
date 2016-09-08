package com.khartec.waltz.jobs;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowStatsDao;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.involvement.InvolvementService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

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

        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference
                        .builder()
                        .kind(EntityKind.APPLICATION)
                        .id(6665)
                        .build())
                .scope(HierarchyQueryScope.EXACT)
                .build();


        Select<Record1<Long>> selector = factory.apply(options);

        System.out.println(selector);

        Result<Record1<Long>> fetch = dsl.fetch(selector);

        fetch.forEach(r -> System.out.println(r.value1()));

        List<DataFlow> r = service.findBySelector(options);
        System.out.println(r);
    }


}
