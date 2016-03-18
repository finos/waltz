package com.khartec.waltz.data.person.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaPersonSearch implements FullTextSearch<Person>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM person\n"
            + " WHERE\n"
            + " MATCH(display_name, user_principal_name, title)\n"
            + " AGAINST (?)\n"
            + " LIMIT 20";


    @Override
    public List<Person> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms);
        return records.map(PersonDao.personMapper);
    }

}
