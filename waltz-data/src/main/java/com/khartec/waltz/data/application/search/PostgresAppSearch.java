package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class PostgresAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    private static final String SEARCH_POSTGRES
            = "SELECT *, "
            + " ts_rank_cd(setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(asset_code, '')), 'A') "
            + "     || setweight(to_tsvector(coalesce(parent_asset_code, '')), 'A'), "
            + "     plainto_tsquery(?)"
            + " ) AS rank"
            + " FROM application"
            + " WHERE setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(asset_code, '')), 'A') "
            + "     || setweight(to_tsvector(coalesce(parent_asset_code, '')), 'A') "
            + "     @@ plainto_tsquery(?)"
            + " ORDER BY rank DESC"
            + " LIMIT 20";


    @Override
    public List<Application> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(SEARCH_POSTGRES, terms, terms);
        return records.map(ApplicationDao.applicationRecordMapper);
    }

}
