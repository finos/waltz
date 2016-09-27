package com.khartec.waltz.data.capability.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.capability.Capability;
import org.jooq.DSLContext;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;

public class SqlServerCapabilitySearch implements FullTextSearch<Capability>, DatabaseVendorSpecific {

    @Override
    public List<Capability> search(DSLContext dsl, String query) {
        List<String> terms = mkTerms(query);
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        return dsl.select(CAPABILITY.fields())
                .from(CAPABILITY)
                .where(JooqUtilities.MSSQL.mkContains(terms))
                .limit(20)
                .fetch(CapabilityDao.TO_DOMAIN_MAPPER);
    }

}
