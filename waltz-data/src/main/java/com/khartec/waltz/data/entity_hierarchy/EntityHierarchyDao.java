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

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_hierarchy.EntityHierarchyItem;
import com.khartec.waltz.model.entity_hierarchy.ImmutableEntityHierarchyItem;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.EntityHierarchy;
import com.khartec.waltz.schema.tables.records.ApplicationRecord;
import com.khartec.waltz.schema.tables.records.EntityHierarchyRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.data.JooqUtilities.TO_STRING_TALLY;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

@Repository
public class EntityHierarchyDao {

    private static final Logger LOG = LoggerFactory.getLogger(EntityHierarchyDao.class);

    private static final EntityHierarchy eh = ENTITY_HIERARCHY;

    private static final Function<EntityHierarchyItem, EntityHierarchyRecord> ITEM_TO_RECORD_MAPPER =
            item -> new EntityHierarchyRecord(
                    item.kind().name(),
                    item.id().get(),
                    item.parentId().orElse(null),
                    item.level());

    public static final RecordMapper<Record, EntityHierarchyItem> TO_DOMAIN_MAPPER = record -> {
        EntityHierarchyRecord ehRecord = record.into(ENTITY_HIERARCHY);
        EntityHierarchyItem item = ImmutableEntityHierarchyItem.builder()
                .id(ehRecord.getId())
                .kind(Enum.valueOf(EntityKind.class, ehRecord.getKind()))
                .parentId(ehRecord.getAncestorId())
                .level(ehRecord.getLevel())
                .build();

        return item;
    };

    private final DSLContext dsl;

    @Autowired
    public EntityHierarchyDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public int replaceHierarchy(EntityKind kind, List<EntityHierarchyItem> hierarchyItems, Condition deleteFilter) {
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(hierarchyItems, "hierarchyItems cannot be null");

        List<EntityHierarchyRecord> records = map(hierarchyItems, ITEM_TO_RECORD_MAPPER);

        LOG.info("Replacing hierarchy items for kind: {}, inserting new record (#{})", kind, hierarchyItems.size());
        dsl.transaction(configuration -> {
            DSLContext txDsl = DSL.using(configuration);
            txDsl.deleteFrom(ENTITY_HIERARCHY)
                    .where(ENTITY_HIERARCHY.KIND.eq(kind.name()))
                    .and(deleteFilter)
                    .execute();
            txDsl.batchInsert(records)
                    .execute();
        });

        return records.size();

    }


    public List<Tally<String>> tallyByKind() {
        return JooqUtilities.calculateStringTallies(dsl, eh, eh.KIND, DSL.trueCondition());
    }


    public List<Tally<String>> getRootTallies() {
        return dsl.select(eh.KIND, DSL.count())
                .from(eh)
                .where(eh.LEVEL.eq(1)
                        .and(eh.ID.eq(eh.ANCESTOR_ID)))
                .groupBy(eh.KIND)
                .fetch(TO_STRING_TALLY);
    }


    public List<EntityHierarchyItem> findDesendents(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl
                .selectFrom(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(ref.kind().name()))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
