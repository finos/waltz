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

package com.khartec.waltz.data.entity_alias;

import com.khartec.waltz.common.SetUtilities;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;


@Repository
public class EntityAliasDao {


    private static final Logger LOG = LoggerFactory.getLogger(EntityAliasDao.class);
    private static final String PROVENANCE = "waltz";

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

        Set<String> currentAliases = SetUtilities.fromCollection(findAliasesForEntityReference(ref));
        Set<String> requiredAliases = SetUtilities.fromCollection(aliases);

        Set<String> toRemove = SetUtilities.minus(currentAliases, requiredAliases);
        Set<String> toAdd = SetUtilities.minus(requiredAliases, currentAliases);

        dsl.delete(ENTITY_ALIAS)
                .where(constrainByEntityReference(ref))
                .and(ENTITY_ALIAS.ALIAS.in(toRemove))
                .execute();

        List<EntityAliasRecord> records = toAdd
                .stream()
                .map(t -> new EntityAliasRecord(ref.id(), t, ref.kind().name(), PROVENANCE))
                .collect(Collectors.toList());

        return dsl
                .batchInsert(records)
                .execute();
    }


    private Condition constrainByEntityReference(EntityReference ref) {
        return ENTITY_ALIAS.ID.eq(ref.id())
                .and(ENTITY_ALIAS.KIND.eq(ref.kind().name()));
    }
}
