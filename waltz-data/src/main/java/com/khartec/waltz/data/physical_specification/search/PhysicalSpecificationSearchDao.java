/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2017  Waltz open source project
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

package com.khartec.waltz.data.physical_specification.search;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.EntityNameUtilities.mkEntityNameField;
import static com.khartec.waltz.data.JooqUtilities.mkBasicTermSearch;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;

@Repository
public class PhysicalSpecificationSearchDao {

    private static final Field<String> OWNER_NAME_FIELD = mkEntityNameField(
            PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID,
            PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecificationSearchDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityReference> search(String termsStr, EntitySearchOptions options) {
        List<String> terms = mkTerms(termsStr);

        Condition likeName = mkBasicTermSearch(PHYSICAL_SPECIFICATION.NAME, terms);
        Condition likeDesc = mkBasicTermSearch(PHYSICAL_SPECIFICATION.NAME, terms);

        return dsl.selectDistinct(
                    PHYSICAL_SPECIFICATION.ID,
                    PHYSICAL_SPECIFICATION.NAME,
                    PHYSICAL_SPECIFICATION.DESCRIPTION,
                    OWNER_NAME_FIELD)
                .from(PHYSICAL_SPECIFICATION)
                .where(likeName)
                .or(likeDesc)
                .orderBy(PHYSICAL_SPECIFICATION.NAME)
                .limit(options.limit())
                .fetch(r -> mkRef(
                        EntityKind.PHYSICAL_SPECIFICATION,
                        r.getValue(PHYSICAL_SPECIFICATION.ID),
                        r.getValue(PHYSICAL_SPECIFICATION.NAME),
                        String.format(
                                "%s %s",
                                Optional.ofNullable(r.getValue(OWNER_NAME_FIELD))
                                        .map(owner -> String.format("(%s)", owner))
                                        .orElse(""),
                                r.getValue(PHYSICAL_SPECIFICATION.DESCRIPTION))));

    }
}
