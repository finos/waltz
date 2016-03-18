package com.khartec.waltz.data;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.emptyList;

public class UnsupportedSearcher<T> implements FullTextSearch<T> {

    private static final Logger LOG = LoggerFactory.getLogger(UnsupportedSearcher.class);

    private final SQLDialect dialect;


    public UnsupportedSearcher(SQLDialect dialect) {
        this.dialect = dialect;
    }


    @Override
    public List<T> search(DSLContext dsl, String terms) {
        LOG.error("Search not supported/implemented for database dialect: " + dialect);
        return emptyList();
    }

}
