package com.khartec.waltz.data.person.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import org.jooq.DSLContext;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Person.PERSON;

public class SqlServerPersonSearch implements FullTextSearch<Person>, DatabaseVendorSpecific {

    @Override
    public List<Person> search(DSLContext dsl, String query) {
        List<String> terms = mkTerms(query);
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        return dsl.select(PERSON.fields())
                .from(PERSON)
                .where(JooqUtilities.MSSQL.mkContains(terms))
                .limit(20)
                .fetch(PersonDao.personMapper);
    }
}
