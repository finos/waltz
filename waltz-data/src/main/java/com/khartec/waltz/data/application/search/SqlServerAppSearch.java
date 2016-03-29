package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import org.jooq.DSLContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;

public class SqlServerAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> search(DSLContext dsl, String terms) {
        return dsl.select(APPLICATION.fields())
                .from(APPLICATION)
                .where(JooqUtilities.MSSQL.mkContains(terms.split(" ")))
                .limit(20)
                .fetch(ApplicationDao.applicationRecordMapper);
    }

}
