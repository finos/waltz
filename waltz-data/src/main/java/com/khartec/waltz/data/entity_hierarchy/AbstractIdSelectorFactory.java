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

package com.khartec.waltz.data.entity_hierarchy;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

public abstract class AbstractIdSelectorFactory implements IdSelectorFactory {

    private final EntityKind entityKind;


    public AbstractIdSelectorFactory(EntityKind entityKind) {
        Checks.checkNotNull(entityKind, "entityKind cannot be null");
        this.entityKind = entityKind;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        EntityKind queryKind = options.entityReference().kind();
        if (entityKind == queryKind) {
            return mkForSelf(options);
        } else {
            return mkForOptions(options);
        }
    }


    protected abstract Select<Record1<Long>> mkForOptions(IdSelectionOptions options);


    private Select<Record1<Long>> mkForSelf(IdSelectionOptions options) {

        Select<Record1<Long>> selector = null;
        switch (options.scope()) {
            case EXACT:
                selector = DSL.select(DSL.val(options.entityReference().id()));
                break;
            case CHILDREN:
                selector = DSL.select(ENTITY_HIERARCHY.ID)
                        .from(ENTITY_HIERARCHY)
                        .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(options.entityReference().id()))
                        .and(ENTITY_HIERARCHY.KIND.eq(entityKind.name()));
                break;
            case PARENTS:
                selector = DSL.select(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .from(ENTITY_HIERARCHY)
                        .where(ENTITY_HIERARCHY.ID.eq(options.entityReference().id()))
                        .and(ENTITY_HIERARCHY.KIND.eq(entityKind.name()));
                break;
        }

        return selector;
    }
}
