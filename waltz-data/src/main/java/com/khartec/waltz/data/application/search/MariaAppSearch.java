package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM application\n"
            + " WHERE\n"
            + "  MATCH(name, description, asset_code, parent_asset_code)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";

    @Override
    public List<Application> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms);
        return records.map(ApplicationDao.applicationRecordMapper);
    }

}
