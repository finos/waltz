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

package com.khartec.waltz.data;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;

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
                tail.forEach(part -> firstPart.union(part));
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
