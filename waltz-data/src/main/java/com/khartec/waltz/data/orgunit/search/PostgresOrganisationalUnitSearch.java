/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.data.orgunit.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.List;

import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


public class PostgresOrganisationalUnitSearch implements FullTextSearch<OrganisationalUnit>, DatabaseVendorSpecific {

    @Override
    public List<OrganisationalUnit> searchFullText(DSLContext dsl, EntitySearchOptions options) {

        Field<Double> rank = DSL
                .field("ts_rank_cd(to_tsvector({0}), plainto_tsquery({1}))",
                        Double.class,
                        ORGANISATIONAL_UNIT.DESCRIPTION.lower(),
                        DSL.inline(options.searchQuery().toLowerCase()));

        return dsl
                .select(ORGANISATIONAL_UNIT.fields())
                .select(rank)
                .from(ORGANISATIONAL_UNIT)
                .where(rank.greaterThan(Double.MIN_VALUE))
                .orderBy(rank.desc())
                .limit(options.limit())
                .fetch(OrganisationalUnitDao.TO_DOMAIN_MAPPER);
    }

}
