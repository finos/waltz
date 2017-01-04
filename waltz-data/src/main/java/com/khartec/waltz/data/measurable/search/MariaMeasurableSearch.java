package com.khartec.waltz.data.measurable.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaMeasurableSearch implements FullTextSearch<Measurable>, DatabaseVendorSpecific {

    private static final String QUERY
            = "SELECT * FROM measurable\n"
            + " WHERE\n"
            + "  MATCH(name, description, external_id)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";


    @Override
    public List<Measurable> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms);
        return records.map(MeasurableDao.TO_DOMAIN_MAPPER);
    }

}
