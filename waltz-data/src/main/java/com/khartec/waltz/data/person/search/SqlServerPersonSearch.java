package com.khartec.waltz.data.person.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import org.jooq.DSLContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.Person.PERSON;

public class SqlServerPersonSearch implements FullTextSearch<Person>, DatabaseVendorSpecific {

    @Override
    public List<Person> search(DSLContext dsl, String terms) {
        return dsl.select(PERSON.fields())
                .from(PERSON)
                .where(JooqUtilities.MSSQL.mkContains(terms.split(" ")))
                .limit(20)
                .fetch(PersonDao.personMapper);
    }
}
