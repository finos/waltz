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

package com.khartec.waltz.data.measurable.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.orderedUnion;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static java.util.Collections.emptyList;

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
