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

package org.finos.waltz.data.entity_hierarchy;

import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.Person;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.function.Function;

import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


public class EntityRootsSelectorFactory implements Function<EntityKind, Select<Record1<Long>>> {

    private static final EntityHierarchy eh = ENTITY_HIERARCHY;
    private static final Person p = PERSON;


    @Override
    public Select<Record1<Long>> apply(EntityKind entityKind) {
        if (entityKind == EntityKind.PERSON) {
            return DSL
                    .select(p.ID)
                    .from(p)
                    .where(p.MANAGER_EMPLOYEE_ID.isNull())
                    .and(p.IS_REMOVED.isFalse());
        } else {
            return DSL
                .select(eh.ID)
                .from(eh)
                .where(eh.LEVEL.eq(1)
                    .and(eh.ID.eq(eh.ANCESTOR_ID)
                        .and(eh.KIND.eq(entityKind.name()))));
        }

    }

}
