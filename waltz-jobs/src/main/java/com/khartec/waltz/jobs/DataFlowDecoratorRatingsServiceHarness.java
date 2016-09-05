package com.khartec.waltz.jobs;

import com.khartec.waltz.model.*;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorRatingsService;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;

/**
 * Created by dwatkins on 03/09/2016.
 */
public class DataFlowDecoratorRatingsServiceHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DataFlowDecoratorRatingsService ratingService = ctx.getBean(DataFlowDecoratorRatingsService.class);
        DataFlowDecoratorService decoratorService = ctx.getBean(DataFlowDecoratorService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        long flowId = 12628L;
        List<DataFlowDecorator> decorators = decoratorService.findByFlowIds(newArrayList(flowId));

        System.out.println(ratingService.calculateRatings(decorators));

        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 50))
                .scope(HierarchyQueryScope.CHILDREN)
                .build();


        System.out.println(decoratorService.summarizeForSelector(options));

    }

}
