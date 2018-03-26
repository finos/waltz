/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.orderedUnion;
import static com.khartec.waltz.data.SearchUtilities.mkRelevancyComparator;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;

public class SqlServerAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> search(DSLContext dsl, String query, EntitySearchOptions options) {
        List<String> terms = mkTerms(query);

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition lifecycleCondition = APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        Condition assetCodeCondition = terms.stream()
                .map(term -> APPLICATION.ASSET_CODE.like(term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaAssetCode = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .where(assetCodeCondition)
                .and(lifecycleCondition)
                .orderBy(APPLICATION.NAME)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

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
                .and(lifecycleCondition)
                .orderBy(APPLICATION.NAME)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

        Condition nameCondition = terms.stream()
                .map(term -> APPLICATION.NAME.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaName = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .where(nameCondition)
                .and(lifecycleCondition)
                .orderBy(APPLICATION.NAME)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);


        List<Application> appsViaFullText = dsl.select(APPLICATION.fields())
                .from(APPLICATION)
                .where(JooqUtilities.MSSQL.mkContainsPrefix(terms))
                .and(lifecycleCondition)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

        appsViaName.sort(mkRelevancyComparator(a -> a.name(), terms.get(0)));

        return new ArrayList<>(orderedUnion(appsViaAssetCode, appsViaName, appsViaAlias, appsViaFullText));
    }

}
