package com.khartec.waltz.data.orgunit.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class OrganisationalUnitSearchDao {

    private final DSLContext dsl;
    private final FullTextSearch<OrganisationalUnit> searcher;


    @Autowired
    public OrganisationalUnitSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<OrganisationalUnit> search(String terms) {
        if (StringUtilities.isEmpty(terms)) {
            return Collections.emptyList();
        }

        return searcher.search(dsl, terms);
    }


    private FullTextSearch<OrganisationalUnit> determineSearcher(SQLDialect dialect) {

        if (dialect == SQLDialect.POSTGRES) {
            return new PostgresOrganisationalUnitSearch();
        }

        if (dialect == SQLDialect.MARIADB) {
            return new MariaOrganisationalUnitSearch();
        }

        // cannot do direct comparison as may not be present.
        if (dialect.name().equals("SQLSERVER2014")) {
            return new SqlServerOrganisationalUnitSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
