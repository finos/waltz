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

package org.finos.waltz.data.measurable.search;

import org.finos.waltz.data.DatabaseVendorSpecific;
import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.measurable.Measurable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.SetUtilities.orderedUnion;
import static org.finos.waltz.data.SearchUtilities.mkTerms;

public class MariaMeasurableSearch implements FullTextSearch<Measurable>, DatabaseVendorSpecific {

    private static final String FULL_TEXT_QUERY
            = "SELECT * FROM measurable\n"
            + " WHERE\n"
            + "  MATCH(name, description, external_id)\n"
            + "  AGAINST (?)\n"
            + "  AND ?\n" // lifecycle condition
            + " LIMIT ?";


    @Override
    public List<Measurable> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition externalIdCondition = terms.stream()
                .map(term -> MEASURABLE.EXTERNAL_ID.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        Condition entityLifecycleCondition = MEASURABLE.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        List<Measurable> measurablesViaExternalId = dsl.selectDistinct(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(externalIdCondition)
                .and(entityLifecycleCondition)
                .orderBy(MEASURABLE.EXTERNAL_ID)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);

        Condition nameCondition = terms.stream()
                .map(term -> MEASURABLE.NAME.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Measurable> measurablesViaName = dsl.selectDistinct(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(nameCondition)
                .and(entityLifecycleCondition)
                .orderBy(MEASURABLE.NAME)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);

        List<Measurable> measurablesViaFullText = dsl.fetch(FULL_TEXT_QUERY, options.searchQuery(), entityLifecycleCondition, options.limit())
                .map(MeasurableDao.TO_DOMAIN_MAPPER);

        return new ArrayList<>(orderedUnion(measurablesViaExternalId, measurablesViaName, measurablesViaFullText));
    }

}
