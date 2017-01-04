package com.khartec.waltz.data.measurable.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class MeasurableSearchDao {


    private final DSLContext dsl;
    private final FullTextSearch<Measurable> searcher;


    @Autowired
    public MeasurableSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<Measurable> search(String terms) {
        if (StringUtilities.isEmpty(terms)) {
            return Collections.emptyList();
        }

        return searcher.search(dsl, terms);
    }


    private FullTextSearch<Measurable> determineSearcher(SQLDialect dialect) {

        if (dialect == SQLDialect.POSTGRES) {
            return new PostgresMeasurableSearch();
        }

        if (dialect == SQLDialect.MARIADB) {
            return new MariaMeasurableSearch();
        }

        if (dialect.name().startsWith("SQLSERVER")) {
            return new SqlServerMeasurableSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
