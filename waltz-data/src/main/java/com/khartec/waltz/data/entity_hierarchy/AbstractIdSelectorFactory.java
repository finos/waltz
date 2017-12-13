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

package com.khartec.waltz.data.entity_hierarchy;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

public abstract class AbstractIdSelectorFactory implements IdSelectorFactory {

    private final DSLContext dsl;
    private final EntityKind entityKind;


    public AbstractIdSelectorFactory(DSLContext dsl, EntityKind entityKind) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        Checks.checkNotNull(entityKind, "entityKind cannot be null");

        this.dsl = dsl;
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
