package com.khartec.waltz.data.measurable.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class PostgresMeasurableSearch implements FullTextSearch<Measurable>, DatabaseVendorSpecific {

    private static final String SEARCH_POSTGRES
            = "SELECT *, "
            + " ts_rank_cd(setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(external_id, '')), 'A') "
            + "     plainto_tsquery(?)"
            + " ) AS rank"
            + " FROM measurable"
            + " WHERE setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(external_id, '')), 'A') "
            + "     @@ plainto_tsquery(?)"
            + " ORDER BY rank DESC"
            + " LIMIT 20";


    @Override
    public List<Measurable> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(SEARCH_POSTGRES, terms, terms);
        return records.map(MeasurableDao.TO_DOMAIN_MAPPER);
    }

}
