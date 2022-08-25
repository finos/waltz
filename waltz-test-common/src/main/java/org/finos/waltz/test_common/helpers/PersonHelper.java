package org.finos.waltz.test_common.helpers;

import org.finos.waltz.data.user.UserDao;
import org.finos.waltz.model.person.PersonKind;
import org.finos.waltz.schema.tables.records.PersonRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static org.finos.waltz.schema.tables.Person.PERSON;

@Service
public class PersonHelper {

    private static final AtomicLong ctr = new AtomicLong();

    @Autowired
    private DSLContext dsl;

    @Autowired
    private UserDao userDao;


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


    public boolean updateIsRemoved(Long pId, boolean isRemoved) {
        int execute = dsl.update(PERSON)
                .set(PERSON.IS_REMOVED, isRemoved)
                .where(PERSON.ID.eq(pId))
                .execute();

        return execute == 1;
    }


    public Long createAdmin() {
        PersonRecord p = dsl.newRecord(PERSON);
        p.setDepartmentName("dept");
        p.setEmail("admin@email.com");
        p.setKind(PersonKind.EMPLOYEE.name());
        p.setDisplayName("Admin");
        p.setEmployeeId(Long.toString(1));
        p.insert();

        userDao.create("admin@email.com", "testUserPassword");

        return p.getId();
    }

}
