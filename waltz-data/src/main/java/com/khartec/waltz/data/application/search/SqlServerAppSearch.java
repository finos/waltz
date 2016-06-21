package com.khartec.waltz.data.application.search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.SetUtilities.union;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationAlias.APPLICATION_ALIAS;

public class SqlServerAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> search(DSLContext dsl, String query) {
        List<String> terms = Stream.of(query.split(" "))
                .filter(StringUtilities::notEmpty)
                .filter(s -> s.length() > 2)
                .collect(Collectors.toList());

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition aliasCondition = terms.stream()
                .map(term -> APPLICATION_ALIAS.ALIAS.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaAlias = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .innerJoin(APPLICATION_ALIAS)
                .on(APPLICATION_ALIAS.APPLICATION_ID.eq(APPLICATION.ID))
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
