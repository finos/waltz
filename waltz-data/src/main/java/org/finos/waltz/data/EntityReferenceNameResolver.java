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

package org.finos.waltz.data;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;

/**
 * Service which takes a list of entity references and returns a list
 * enriched with entity names.
 */
@Repository
public class EntityReferenceNameResolver {

    private final DSLContext dsl;

    @Autowired
    public EntityReferenceNameResolver(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<EntityReference> resolve(EntityReference ref) {
        return maybeFirst(resolve(newArrayList(ref)));
    }

    public List<EntityReference> resolve(List<EntityReference> refs) {
        checkNotNull(refs, "refs cannot be null");
        
        Field<Long> idField = DSL.field("tref_id", Long.class);
        Field<String> kindField = DSL.field("tref_kind", String.class);

        Field<String> nameField = InlineSelectFieldFactory.mkNameField(
                idField,
                kindField);

        List<SelectSelectStep<Record2<String, Long>>> parts = map(
                refs,
                r -> DSL.select(
                        DSL.val(r.kind().name()).as(kindField),
                        DSL.val(r.id()).as(idField)));


        if (parts.size() > 0) {
            SelectSelectStep<Record2<String, Long>> firstPart = parts.get(0);
            if (parts.size() > 1) {
                List<SelectSelectStep<Record2<String, Long>>> tail = ListUtilities.drop(parts, 1);
                tail.forEach(firstPart::union);
            }

            Table<Record2<String, Long>> tempRefs = firstPart.asTable("temp_refs");
            return dsl
                    .select(idField, kindField, nameField)
                    .from(tempRefs)
                    .fetch(r -> mkRef(
                            EntityKind.valueOf(r.get(kindField)),
                            r.get(idField),
                            r.get(nameField)));
        } else {
            return Collections.emptyList();
        }

    }

}
