package com.khartec.waltz.data.capability.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.capability.Capability;
import org.jooq.DSLContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;

public class SqlServerCapabilitySearch implements FullTextSearch<Capability>, DatabaseVendorSpecific {

    @Override
    public List<Capability> search(DSLContext dsl, String terms) {
        return dsl.select(CAPABILITY.fields())
                .from(CAPABILITY)
                .where(JooqUtilities.MSSQL.mkContains(terms.split(" ")))
                .limit(20)
                .fetch(CapabilityDao.capabilityMapper);
    }

}
