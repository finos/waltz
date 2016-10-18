package com.khartec.waltz.jobs;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class SpecificationHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        PhysicalSpecificationDao physicalSpecificationDao = ctx.getBean(PhysicalSpecificationDao.class);

        ProduceConsumeGroup<PhysicalSpecification> specifications = physicalSpecificationDao.findByAppId(67502);
        System.out.println(specifications);
    }

}
