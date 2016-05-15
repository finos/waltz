package com.khartec.waltz.jobs;

import com.khartec.waltz.data.complexity.ServerComplexityDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.service.complexity.ServerComplexityService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.common.ArrayUtilities.mapToList;
import static com.khartec.waltz.model.utils.IdUtilities.toIdArray;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


public class ServerComplexityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        OrganisationalUnitDao ouDao = ctx.getBean(OrganisationalUnitDao.class);
        ServerComplexityDao serverDao = ctx.getBean(ServerComplexityDao.class);
        ServerComplexityService serverService = ctx.getBean(ServerComplexityService.class);

        ComplexityRatingService complexityService = ctx.getBean(ComplexityRatingService.class);


    }


    private static Select<Record1<Long>> byPhase(LifecyclePhase... phases) {
        return DSL.select(APPLICATION.ID)
                        .from(APPLICATION)
                        .where(APPLICATION.LIFECYCLE_PHASE.in(mapToList(phases, p -> p.name())));
    }


    private static Select<Record1<Long>> byOrgUnit(Long... orgUnits) {
        return DSL.select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnits));
    }


    private static Select<Record1<Long>> byOrgUnitTree(Long unitId, OrganisationalUnitDao ouDao) {
        return byOrgUnit(toIdArray(ouDao.findDescendants(unitId)));
    }


    private static void measureDuration(Runnable r) {
        long st = System.currentTimeMillis();
        r.run();
        System.out.println("Duration: "+(System.currentTimeMillis() - st));
    }

    private static void sout(Object o) {
        System.out.println(o);
    }

}
