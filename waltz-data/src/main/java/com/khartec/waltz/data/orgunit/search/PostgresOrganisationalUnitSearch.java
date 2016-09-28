package com.khartec.waltz.data.orgunit.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;


public class PostgresOrganisationalUnitSearch implements FullTextSearch<OrganisationalUnit>, DatabaseVendorSpecific {

    private static final String QUERY = "SELECT\n" +
            "  *,\n" +
            "  ts_rank_cd(\n" +
            "      setweight(to_tsvector(name), 'A')\n" +
            "      || setweight(to_tsvector(description), 'D'),\n" +
            "      plainto_tsquery(?)) AS rank\n" +
            "FROM organisational_unit\n" +
            "WHERE\n" +
            "  setweight(to_tsvector(name), 'A')\n" +
            "  || setweight(to_tsvector(description), 'D')\n" +
            "  @@ plainto_tsquery(?)\n" +
            "ORDER BY rank DESC\n" +
            "LIMIT 20;\n";


    @Override
    public List<OrganisationalUnit> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms, terms);
        return records.map(OrganisationalUnitDao.TO_DOMAIN_MAPPER);
    }

}
