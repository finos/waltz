package com.khartec.waltz.data.capability.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.capability.Capability;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class PostgresCapabilitySearch implements FullTextSearch<Capability>, DatabaseVendorSpecific {


    private static final String QUERY = "SELECT\n" +
            "  *,\n" +
            "  ts_rank_cd(\n" +
            "      setweight(to_tsvector(name), 'A')\n" +
            "      || setweight(to_tsvector(description), 'D'),\n" +
            "      plainto_tsquery(?)) AS rank\n" +
            "FROM capability\n" +
            "WHERE\n" +
            "  setweight(to_tsvector(name), 'A')\n" +
            "  || setweight(to_tsvector(description), 'D')\n" +
            "  @@ plainto_tsquery(?)\n" +
            "ORDER BY rank DESC\n" +
            "LIMIT 20;\n";


    @Override
    public List<Capability> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms, terms);
        return records.map(CapabilityDao.TO_DOMAIN_MAPPER);
    }

}
