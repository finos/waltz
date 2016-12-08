package com.khartec.waltz.jobs;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.service.complexity.ServerComplexityService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class ServerComplexityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        OrganisationalUnitDao ouDao = ctx.getBean(OrganisationalUnitDao.class);
        ServerComplexityService serverService = ctx.getBean(ServerComplexityService.class);

        ComplexityRatingService complexityService = ctx.getBean(ComplexityRatingService.class);

//        EntityReference entityReference = EntityReference.mkRef(EntityKind.PERSON, 1);
        EntityReference entityReference = EntityReference.mkRef(EntityKind.PERSON, 63);

        List<ComplexityRating> complexity = FunctionUtilities.time("complexity",
                () -> complexityService.findForAppIdSelector(IdSelectionOptions.mkOpts(entityReference, HierarchyQueryScope.CHILDREN)));


    }


}
