package com.khartec.waltz.jobs;

import com.khartec.waltz.data.complexity.CapabilityComplexityDao;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.complexity.CapabilityComplexityService;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class CapabilityComplexityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        CapabilityComplexityDao capabilityComplexityDao =
                ctx.getBean(CapabilityComplexityDao.class);

        CapabilityComplexityService capabilityComplexityService =
                ctx.getBean(CapabilityComplexityService.class);

        ComplexityRatingService complexityRatingService = ctx.getBean(ComplexityRatingService.class);

        sout(capabilityComplexityDao.findBaseline());

        System.out.println(capabilityComplexityService.getForApp(126L));

        System.out.println(complexityRatingService.getForApp(126L));
        System.out.println(complexityRatingService.findWithinOrgUnitTree(220L).size());
//        sout(capabilityComplexityDao.findBaseLineForOrgUnitIds(100L, 120L));
//        sout(capabilityComplexityDao.findScoresForOrgUnitIds(120L));
//        sout(capabilityComplexityService.findWithinOrgUnit(100L));
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
