package com.khartec.waltz.data.person.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;


public class PostgresPersonSearch implements FullTextSearch<Person>, DatabaseVendorSpecific {

    private static final String QUERY
            = "SELECT *,\n"
            + "  ts_rank_cd(setweight(to_tsvector(coalesce(display_name, '')), 'A')\n"
            + "             || setweight(to_tsvector(coalesce(title, '')), 'D'),\n"
            + "             plainto_tsquery(?) )\n"
            + "    AS rank\n"
            + "FROM person\n"
            + "WHERE\n"
            + "  setweight(to_tsvector(coalesce(display_name, '')), 'A')\n"
            + "  || setweight(to_tsvector(coalesce(title, '')), 'D')\n"
            + "  @@ plainto_tsquery(?)\n"
            + "ORDER BY rank\n"
            + "DESC LIMIT 20;";


    @Override
    public List<Person> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms, terms);
        return records.map(PersonDao.personMapper);
    }

}
