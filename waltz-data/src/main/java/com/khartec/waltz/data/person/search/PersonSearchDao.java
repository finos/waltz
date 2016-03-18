package com.khartec.waltz.data.person.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.model.person.Person;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class PersonSearchDao {

    private final DSLContext dsl;
    private final FullTextSearch<Person> searcher;


    @Autowired
    public PersonSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<Person> search(String terms) {
        if (StringUtilities.isEmpty(terms)) {
            return Collections.emptyList();
        }

        return searcher.search(dsl, terms);
    }


    private FullTextSearch<Person> determineSearcher(SQLDialect dialect) {

        if (dialect == SQLDialect.POSTGRES) {
            return new PostgresPersonSearch();
        }

        if (dialect == SQLDialect.MARIADB) {
            return new MariaPersonSearch();
        }

        if (dialect.name().equals("SQLSERVER2014")) {
            return new SqlServerPersonSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
