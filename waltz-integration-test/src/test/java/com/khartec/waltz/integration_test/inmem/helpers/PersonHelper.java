package com.khartec.waltz.integration_test.inmem.helpers;

import org.finos.waltz.model.person.PersonKind;
import com.khartec.waltz.schema.tables.records.PersonRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.schema.tables.Person.PERSON;

@Service
public class PersonHelper {

    private static final AtomicLong ctr = new AtomicLong();

    private final DSLContext dsl;

    @Autowired
    public PersonHelper(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Long createPerson(String name) {
        PersonRecord p = dsl.newRecord(PERSON);
        p.setDepartmentName("dept");
        p.setEmail(name);
        p.setKind(PersonKind.EMPLOYEE.name());
        p.setDisplayName(name);
        p.setEmployeeId(Long.toString(ctr.incrementAndGet()));
        p.insert();

        return p.getId();
    }
}
