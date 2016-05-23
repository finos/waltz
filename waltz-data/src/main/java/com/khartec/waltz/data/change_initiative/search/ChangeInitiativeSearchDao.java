package com.khartec.waltz.data.change_initiative.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ChangeInitiativeSearchDao {


    private final DSLContext dsl;
    private final FullTextSearch<ChangeInitiative> searcher;


    @Autowired
    public ChangeInitiativeSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<ChangeInitiative> search(String terms) {
        if (StringUtilities.isEmpty(terms)) {
            return Collections.emptyList();
        }

        return searcher.search(dsl, terms);
    }


    private FullTextSearch<ChangeInitiative> determineSearcher(SQLDialect dialect) {

        if (dialect == SQLDialect.POSTGRES) {
            return new PostgresChangeInitiativeSearch();
        }

        if (dialect == SQLDialect.MARIADB) {
            return new MariaChangeInitiativeSearch();
        }

        if (dialect.name().startsWith("SQLSERVER")) {
            return new SqlServerChangeInitiativeSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
