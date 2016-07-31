package com.khartec.waltz.jobs;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by dwatkins on 30/07/2016.
 */
public class EntityHierarchyHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        EntityHierarchyService svc = ctx.getBean(EntityHierarchyService.class);
        svc.buildFor(EntityKind.ORG_UNIT);
        svc.buildFor(EntityKind.CAPABILITY);
        svc.buildFor(EntityKind.PROCESS);
        svc.buildFor(EntityKind.ENTITY_STATISTIC);
    }
}
