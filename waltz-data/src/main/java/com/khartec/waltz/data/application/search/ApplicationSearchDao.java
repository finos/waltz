package com.khartec.waltz.data.application.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.model.application.Application;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ApplicationSearchDao {


    private final DSLContext dsl;
    private final FullTextSearch<Application> searcher;


    @Autowired
    public ApplicationSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<Application> search(String terms) {
        if (StringUtilities.isEmpty(terms)) {
            return Collections.emptyList();
        }

        return searcher.search(dsl, terms);
    }


    private FullTextSearch<Application> determineSearcher(SQLDialect dialect) {

        if (dialect == SQLDialect.POSTGRES) {
            return new PostgresAppSearch();
        }

        if (dialect == SQLDialect.MARIADB) {
            return new MariaAppSearch();
        }

        if (dialect.name().equals("SQLSERVER2014")) {
            return new SqlServerAppSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
