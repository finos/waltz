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

package com.khartec.waltz.data.roadmap;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.impl.DSL.selectDistinct;

/**
 * Roadmaps are typically associated with entities in one of two ways:
 *
 * <ul>
 *      <li>directly (via the `entity_relationship` table)</li>
 *      <li>implicitly (via the scenario axis definitions and ratings)</li>
 * </ul>
 *
 *  Note, currently, this selector factory only supports the first of these options.
 */
public class RoadmapIdSelectorFactory implements IdSelectorFactory {


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {

        switch (options.entityReference().kind()) {
            case ORG_UNIT:
            case PERSON:
            case APP_GROUP:
                return mkViaRelationships(options);
            default:
                // update class comment if list of supported entities changes
                String msg = String.format(
                        "Cannot create Change Initiative Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }


    private Select<Record1<Long>> mkViaRelationships(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        return selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.ROADMAP.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()));
    }

}
