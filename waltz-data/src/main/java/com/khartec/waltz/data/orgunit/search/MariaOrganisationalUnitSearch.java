package com.khartec.waltz.data.orgunit.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

/**
 * Created by dwatkins on 17/03/2016.
 */
public class MariaOrganisationalUnitSearch implements FullTextSearch<OrganisationalUnit>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM organisational_unit\n"
            + " WHERE\n"
            + "  MATCH(name, description)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";


    @Override
    public List<OrganisationalUnit> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms);
        return records.map(OrganisationalUnitDao.recordMapper);
    }

}
