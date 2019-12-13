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
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;

public class MariaAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> tokens = map(mkTerms(options.searchQuery()), t -> t.toLowerCase());
        Condition lifecycleCondition = APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        return dsl
                .select(APPLICATION.fields())
                .from(APPLICATION)
                .where("MATCH(name, description, asset_code, parent_asset_code) AGAINST (?)", options.searchQuery())
                .and(lifecycleCondition)
                .union(DSL.selectDistinct(APPLICATION.fields())
                                .from(APPLICATION)
                                .innerJoin(ENTITY_ALIAS)
                                .on(ENTITY_ALIAS.ID.eq(APPLICATION.ID)
                                        .and(ENTITY_ALIAS.KIND.eq(EntityKind.APPLICATION.name())))
                                .where(DSL.lower(ENTITY_ALIAS.ALIAS).in(tokens))
                                .and(lifecycleCondition))
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }

}
