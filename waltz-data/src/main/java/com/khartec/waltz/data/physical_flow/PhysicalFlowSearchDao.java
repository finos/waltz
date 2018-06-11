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

package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.Collections.emptyList;


@Repository
public class PhysicalFlowSearchDao {


    private final DSLContext dsl;

    @Autowired
    public PhysicalFlowSearchDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    /**
     * A report is a physical flow which goes to an
     * external actor
     * @param query
     * @return
     */
    public List<EntityReference> searchReports(String query) {

        if (isEmpty(query)) {
            return emptyList();
        }

        Field<String> nameField = DSL.concat(
                PHYSICAL_SPECIFICATION.NAME,
                DSL.value(" - "),
                ACTOR.NAME);

        Condition termMatcher = mkQueryCondition(query, nameField);

        return dsl.select(PHYSICAL_FLOW.ID, nameField)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .innerJoin(ACTOR)
                .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ACTOR.ID).and(ACTOR.IS_EXTERNAL.eq(true)))
                .where(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.ACTOR.name()))
                .and(termMatcher)
                .fetch()
                .stream()
                .map(r -> mkRef(
                        EntityKind.PHYSICAL_FLOW,
                        r.value1(),
                        r.value2()))
                .collect(Collectors.toList());
    }


    private Condition mkQueryCondition(String query, Field<String> nameField) {
        List<String> terms = mkTerms(query);
        Condition termMatcher = DSL.trueCondition();
        terms.forEach(t -> termMatcher.and(nameField.like("%" + t + "%")));
        return termMatcher;
    }

}
