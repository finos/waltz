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

package com.khartec.waltz.data.entity_alias;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.schema.tables.records.EntityAliasRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;


@Repository
public class EntityAliasDao {


    private static final Logger LOG = LoggerFactory.getLogger(EntityAliasDao.class);

    private final DSLContext dsl;


    @Autowired
    public EntityAliasDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<String> findAliasesForEntityReference(EntityReference ref) {
        return dsl.select(ENTITY_ALIAS.ALIAS)
                .from(ENTITY_ALIAS)
                .where(constrainByEntityReference(ref))
                .orderBy(ENTITY_ALIAS.ALIAS.asc())
                .fetch(ENTITY_ALIAS.ALIAS);
    }


    public int[] updateAliases(EntityReference ref, Collection<String> aliases) {

        LOG.info("Updating aliases for entity: {}, aliases: {}", ref, aliases);

        dsl.delete(ENTITY_ALIAS)
                .where(constrainByEntityReference(ref))
                .execute();

        List<EntityAliasRecord> records = aliases.stream()
                .map(t -> new EntityAliasRecord(ref.id(), t, ref.kind().name()))
                .collect(Collectors.toList());

        return dsl.batchInsert(records)
                .execute();
    }


    private Condition constrainByEntityReference(EntityReference ref) {
        return ENTITY_ALIAS.ID.eq(ref.id())
                .and(ENTITY_ALIAS.KIND.eq(ref.kind().name()));
    }
}
