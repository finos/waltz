package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.trait.TraitUsageKind;
import com.khartec.waltz.schema.tables.records.TraitUsageRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;
import static com.khartec.waltz.schema.tables.Trait.TRAIT;
import static com.khartec.waltz.schema.tables.TraitUsage.TRAIT_USAGE;

/**
 * Created by dwatkins on 17/03/2016.
 */
public class TraitGenerator {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        dsl.deleteFrom(TRAIT).execute();
        dsl.deleteFrom(TRAIT_USAGE).execute();

        dsl.insertInto(TRAIT)
                .set(TRAIT.NAME, "Infrastructure")
                .set(TRAIT.APPLICATION_DECLARABLE, true)
                .set(TRAIT.DESCRIPTION, "Infrastructure")
                .set(TRAIT.ICON, "cogs")
                .execute();

        Long traitId = dsl.select(TRAIT.ID)
                .from(TRAIT)
                .where(TRAIT.NAME.eq("Infrastructure"))
                .fetchOne()
                .value1();

        List<TraitUsageRecord> usages = dsl.select(CAPABILITY.ID)
                .from(CAPABILITY)
                .where(CAPABILITY.ID.ge(6000L))
                .and(CAPABILITY.ID.lt(7000L))
                .stream()
                .map(r -> new TraitUsageRecord(
                        EntityKind.CAPABILITY.name(),
                        r.value1(),
                        TraitUsageKind.REQUIRES.name(),
                        traitId))
                .collect(Collectors.toList());

        dsl.batchInsert(usages).execute();


    }
}
