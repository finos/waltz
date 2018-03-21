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

package com.khartec.waltz.data.physical_specification.search;


import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.JooqUtilities.mkBasicTermSearch;
import static com.khartec.waltz.data.SearchUtilities.mkRelevancyComparator;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao.owningEntityNameField;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;

@Repository
public class PhysicalSpecificationSearchDao {

    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecificationSearchDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PhysicalSpecification> search(String termsStr, EntitySearchOptions options) {
        List<String> terms = mkTerms(termsStr);
        if (terms.isEmpty()) {
            return newArrayList();
        }

        Condition likeName = mkBasicTermSearch(PHYSICAL_SPECIFICATION.NAME, terms);
        Condition likeDesc = mkBasicTermSearch(PHYSICAL_SPECIFICATION.DESCRIPTION, terms);

        SelectQuery<Record> query = dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(likeName)
                .or(likeDesc)
                .orderBy(PHYSICAL_SPECIFICATION.NAME)
                .limit(options.limit())
                .getQuery();

        List<PhysicalSpecification> results = query
                .fetch(r -> {
                    PhysicalSpecification spec = PhysicalSpecificationDao.TO_DOMAIN_MAPPER.map(r);
                    String updatedDesc = String.format(
                            "%s %s",
                            Optional.ofNullable(r.getValue(owningEntityNameField))
                                    .map(owner -> String.format("(%s)", owner))
                                    .orElse(""),
                            spec.description());

                    return ImmutablePhysicalSpecification
                            .copyOf(spec)
                            .withDescription(updatedDesc);
                });

        results.sort(mkRelevancyComparator(
                ps -> ps.name(),
                terms.get(0)));

        return results;
    }

}
