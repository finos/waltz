package com.khartec.waltz.jobs;

import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.complexity.ServerComplexityService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class ServerComplexityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        OrganisationalUnitDao ouDao = ctx.getBean(OrganisationalUnitDao.class);
        ServerComplexityService serverService = ctx.getBean(ServerComplexityService.class);


    }


}
