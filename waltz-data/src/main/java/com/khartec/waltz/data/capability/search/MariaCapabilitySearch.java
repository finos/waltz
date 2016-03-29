package com.khartec.waltz.data.capability.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.capability.Capability;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaCapabilitySearch implements FullTextSearch<Capability>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM capability\n"
            + " WHERE\n"
            + "  MATCH(name, description)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";


    @Override
    public List<Capability> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms);
        return records.map(CapabilityDao.capabilityMapper);
    }

}
