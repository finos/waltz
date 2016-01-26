package com.khartec.waltz.jobs;

import com.khartec.waltz.data.complexity.ConnectionComplexityDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.tally.LongTally;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.common.ArrayUtilities.mapToList;
import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;
import static com.khartec.waltz.model.utils.IdUtilities.toIdArray;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


public class ConnectivityComplexityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ConnectionComplexityDao dao = ctx.getBean(ConnectionComplexityDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        OrganisationalUnitDao ouDao = ctx.getBean(OrganisationalUnitDao.class);

        dsl.select()
                .from(DSL.table(new Long[] {1L, 2L, 3L}))
                .forEach(System.out::println);

        System.exit(-1);


        measureDuration(() -> System.out.println(dao.findBaseline()));

        measureDuration(() -> sout(dao.findCounts(byPhase(LifecyclePhase.PRODUCTION)).size()));
        measureDuration(() -> sout(dao.findCounts(byPhase(LifecyclePhase.DEVELOPMENT)).size()));
        measureDuration(() -> sout(dao.findCounts(byOrgUnit(130L)).size()));
        measureDuration(() -> sout(dao.findCounts(byOrgUnitTree(100L, ouDao)).size()));
        measureDuration(() -> sout(dao.findCounts(byOrgUnitTree(10L, ouDao)).size()));
        measureDuration(() -> sout(dao.findCounts(byOrgUnitTree(300L, ouDao)).size()));


        int max = dao.findBaseline();
        List<LongTally> counts = dao.findCounts(byOrgUnitTree(10L, ouDao));

        counts.stream()
                .map(t -> tallyToComplexityScore(t, max))
                .forEach(System.out::println);

        System.out.println("----");
        System.out.println(2.0 / 3);
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
