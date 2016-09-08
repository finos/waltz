package com.khartec.waltz.jobs;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;

/**
 * Created by dwatkins on 03/09/2016.
 */
public class DataFlowDecoratorHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DataFlowDecoratorService service = ctx.getBean(DataFlowDecoratorService.class);

        EntityReference dataType = EntityReference.mkRef(EntityKind.DATA_TYPE, 8000);
        IdSelectionOptions options = IdSelectionOptions.mkOpts(dataType, HierarchyQueryScope.CHILDREN, EntityKind.DATA_TYPE);

        service.findBySelector(options).forEach(System.out::println);


    }

}
