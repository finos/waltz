package com.khartec.waltz.data.capability.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.model.capability.Capability;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class CapabilitySearchDao {


    private final DSLContext dsl;
    private final FullTextSearch<Capability> searcher;


    @Autowired
    public CapabilitySearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<Capability> search(String terms) {
        if (StringUtilities.isEmpty(terms)) {
            return Collections.emptyList();
        }

        return searcher.search(dsl, terms);
    }


    private FullTextSearch<Capability> determineSearcher(SQLDialect dialect) {

        if (dialect == SQLDialect.POSTGRES) {
            return new PostgresCapabilitySearch();
        }

        if (dialect == SQLDialect.MARIADB) {
            return new MariaCapabilitySearch();
        }

        if (dialect.name().equals("SQLSERVER2014")) {
            return new SqlServerCapabilitySearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
