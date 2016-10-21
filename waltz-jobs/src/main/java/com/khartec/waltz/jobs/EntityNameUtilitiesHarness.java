package com.khartec.waltz.jobs;


import com.khartec.waltz.data.EntityNameUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

public class EntityNameUtilitiesHarness {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("without entity names", () -> {
                dsl.selectFrom(ENTITY_STATISTIC_VALUE)
                        .fetch();
                return null;
            });
        }

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("with entity names", () -> {
                Field<String> entityNameField = EntityNameUtilities.mkEntityNameField(
                        ENTITY_STATISTIC_VALUE.ENTITY_ID,
                        ENTITY_STATISTIC_VALUE.ENTITY_KIND,
                        newArrayList(EntityKind.APPLICATION, EntityKind.ORG_UNIT));

                dsl.select(ENTITY_STATISTIC_VALUE.fields())
                        .select(entityNameField)
                        .from(ENTITY_STATISTIC_VALUE)
                        .fetch();
//                        .forEach(System.out::println);
                return null;
            });
        }
    }
}
