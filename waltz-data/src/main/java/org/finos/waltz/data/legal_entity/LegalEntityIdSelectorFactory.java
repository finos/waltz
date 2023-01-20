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

package org.finos.waltz.data.legal_entity;

import org.finos.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import static org.finos.waltz.schema.Tables.LEGAL_ENTITY_RELATIONSHIP;
import static org.jooq.impl.DSL.select;


public class LegalEntityIdSelectorFactory extends AbstractIdSelectorFactory {

    public LegalEntityIdSelectorFactory() {
        super(EntityKind.LEGAL_ENTITY);
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APPLICATION:
                return mkForTargetEntity(options);
            default:
                String msg = String.format(
                        "Cannot create Legal Entity Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }

    private Select<Record1<Long>> mkForTargetEntity(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        return select(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .where(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.eq(ref.id())
                        .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.eq(ref.kind().name())));
    }

}
