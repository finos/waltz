/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.physical_specification.search;


import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.schema.Tables.EXTERNAL_IDENTIFIER;
import static org.finos.waltz.schema.Tables.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.physical_specification.PhysicalSpecificationDao.owningEntityNameField;

@Repository
public class PhysicalSpecificationSearchDao implements SearchDao<PhysicalSpecification> {

    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecificationSearchDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    @Override
    public List<PhysicalSpecification> search(EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return newArrayList();
        }

        Condition likeName = JooqUtilities.mkBasicTermSearch(PHYSICAL_SPECIFICATION.NAME, terms);
        Condition likeDesc = JooqUtilities.mkBasicTermSearch(PHYSICAL_SPECIFICATION.DESCRIPTION, terms);
        Condition likeExternalIdentifier = JooqUtilities.mkStartsWithTermSearch(PHYSICAL_SPECIFICATION.EXTERNAL_ID, terms)
                .or(JooqUtilities.mkStartsWithTermSearch(EXTERNAL_IDENTIFIER.EXTERNAL_ID, terms));

        Condition searchFilter = likeName.or(likeDesc).or(likeExternalIdentifier);

        SelectQuery<Record> query = dsl
                .selectDistinct(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .leftOuterJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .leftOuterJoin(EXTERNAL_IDENTIFIER)
                .on(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())
                    .and(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(PHYSICAL_FLOW.ID)))
                .where(PHYSICAL_SPECIFICATION.IS_REMOVED.eq(false))
                .and(searchFilter)
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

        results.sort(SearchUtilities.mkRelevancyComparator(
                NameProvider::name,
                terms.get(0)));

        return results;
    }

}
