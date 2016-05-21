package com.khartec.waltz.data.change_initiative.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaChangeInitiativeSearch implements FullTextSearch<ChangeInitiative>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM change_initiative\n"
            + " WHERE\n"
            + "  MATCH(name, description, external_id)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";

    @Override
    public List<ChangeInitiative> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms);
        return records.map(ChangeInitiativeDao.TO_DOMAIN_MAPPER);
    }

}
