package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.union;
import static com.khartec.waltz.common.StringUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;

public class SqlServerAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> search(DSLContext dsl, String query) {
        List<String> terms = mkTerms(query);

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition aliasCondition = terms.stream()
                .map(term -> ENTITY_ALIAS.ALIAS.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        ENTITY_ALIAS.KIND.eq(EntityKind.APPLICATION.name()),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaAlias = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .innerJoin(ENTITY_ALIAS)
                .on(ENTITY_ALIAS.ID.eq(APPLICATION.ID))
                .where(aliasCondition)
                .orderBy(APPLICATION.NAME)
                .limit(20)
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

        List<Application> appsViaFullText = dsl.select(APPLICATION.fields())
                .from(APPLICATION)
                .where(JooqUtilities.MSSQL.mkContains(terms))
                .limit(20)
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

        return new ArrayList<>(union(appsViaAlias, appsViaFullText));
    }

}
