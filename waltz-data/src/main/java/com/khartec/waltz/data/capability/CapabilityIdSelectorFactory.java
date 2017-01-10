/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.capability;


import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

@Service
public class CapabilityIdSelectorFactory extends AbstractIdSelectorFactory {


    @Autowired
    public CapabilityIdSelectorFactory(DSLContext dsl) {
        super(dsl, EntityKind.CAPABILITY);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
                return mkForAppGroup(options);
            default:
                throw new UnsupportedOperationException("Cannot create capability selector from kind: "+options.entityReference().kind());
        }
    }

    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        switch (options.scope()) {
            case PARENTS:
                return mkForAppGroupParents(options.entityReference().id());
            default:
                throw new UnsupportedOperationException("Cannot create capability selector from APP_GROUP with scope: "+options.scope());
        }
    }

    private Select<Record1<Long>> mkForAppGroupParents(long id) {
        return DSL.selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .innerJoin(APP_CAPABILITY)
                    .on(APP_CAPABILITY.CAPABILITY_ID.eq(ENTITY_HIERARCHY.ID))
                .innerJoin(APPLICATION_GROUP_ENTRY)
                    .on(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(APP_CAPABILITY.APPLICATION_ID))
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(id));

    }
}
