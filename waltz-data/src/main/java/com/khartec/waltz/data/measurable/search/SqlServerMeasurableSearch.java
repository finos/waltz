package com.khartec.waltz.data.measurable.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.DSLContext;

import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static java.util.Collections.emptyList;

public class SqlServerMeasurableSearch implements FullTextSearch<Measurable>, DatabaseVendorSpecific {

    @Override
    public List<Measurable> search(DSLContext dsl, String query) {
        List<String> terms = mkTerms(query);

        if (terms.isEmpty()) {
            return emptyList();
        }

        return dsl
                .selectFrom(MEASURABLE)
                .where(JooqUtilities.MSSQL.mkContains(terms))
                .limit(20)
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);
    }

}
