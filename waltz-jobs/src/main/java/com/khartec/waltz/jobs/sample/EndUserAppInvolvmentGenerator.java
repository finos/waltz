package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.involvement.InvolvementKind;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


/**
 * Created by dwatkins on 29/09/2016.
 */
public class EndUserAppInvolvmentGenerator {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> appIds = dsl.select(END_USER_APPLICATION.ID)
                .from(END_USER_APPLICATION)
                .fetch(END_USER_APPLICATION.ID);

        List<String> empIds = dsl.select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .innerJoin(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.LEVEL.lt(4))
                .fetch(PERSON.EMPLOYEE_ID);

        List<InvolvementRecord> records = ListUtilities.map(appIds, id -> {
            InvolvementRecord record = dsl.newRecord(INVOLVEMENT);
            record.setProvenance("RANDOM_GENERATOR");
            record.setEmployeeId(randomPick(empIds));
            record.setEntityId(id);
            record.setEntityKind(EntityKind.END_USER_APPLICATION.name());
            record.setKind(ArrayUtilities.randomPick(InvolvementKind.values()).name());
            return record;
        });


        System.out.println("---saving: "+records.size());
        dsl.batchInsert(records).execute();
        System.out.println("---done");
    }
}
