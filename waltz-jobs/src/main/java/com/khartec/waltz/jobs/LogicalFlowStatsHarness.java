package com.khartec.waltz.jobs;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowStatsDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LogicalFlowStatsHarness {
    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LogicalFlowService service = ctx.getBean(LogicalFlowService.class);
        LogicalFlowStatsDao dao = ctx.getBean(LogicalFlowStatsDao.class);
        ApplicationIdSelectorFactory factory = ctx.getBean(ApplicationIdSelectorFactory.class);

        IdSelectionOptions options = IdSelectionOptions.mkOpts(
                EntityReference.mkRef(EntityKind.PERSON, 125613),
                HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> selector = factory.apply(options);
        System.out.println(selector);

        //
        //tallyDataTypesByAppIdSelector
        //
        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("null check optimisation", () -> {
                dao.tallyDataTypesByAppIdSelector(selector);
                return null;
            });
        }

        //
        // countDistinctFlowInvolvementByAppIdSelector
        //
        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("optimised", () -> {
                return dao.countDistinctFlowInvolvementByAppIdSelector(selector);
            });
        }


    }
}
