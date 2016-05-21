package com.khartec.waltz.data.change_initiative.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class PostgresChangeInitiativeSearch implements FullTextSearch<ChangeInitiative>, DatabaseVendorSpecific {

    private static final String SEARCH_POSTGRES
            = "SELECT *, "
            + " ts_rank_cd(setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(external_id, '')), 'A'), "
            + "     plainto_tsquery(?)"
            + " ) AS rank"
            + " FROM change_initiative"
            + " WHERE setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(external_id, '')), 'A') "
            + "     @@ plainto_tsquery(?)"
            + " ORDER BY rank DESC"
            + " LIMIT 20";


    @Override
    public List<ChangeInitiative> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(SEARCH_POSTGRES, terms, terms);
        return records.map(ChangeInitiativeDao.TO_DOMAIN_MAPPER);
    }

}
