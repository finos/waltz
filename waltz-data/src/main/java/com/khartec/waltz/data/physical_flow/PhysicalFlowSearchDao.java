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
