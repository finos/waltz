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
import org.finos.waltz.schema.tables.records.EntityHierarchyRecord;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchyItem;
import org.finos.waltz.model.entity_hierarchy.ImmutableEntityHierarchyItem;
import org.finos.waltz.model.tally.Tally;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.map;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class EntityHierarchyDao {

    private static final Logger LOG = LoggerFactory.getLogger(EntityHierarchyDao.class);

    private static final EntityHierarchy eh = ENTITY_HIERARCHY;

    private static final Function<EntityHierarchyItem, EntityHierarchyRecord> ITEM_TO_RECORD_MAPPER =
            item -> item
                    .id()
                    .map(id -> {
                        EntityHierarchyRecord r = new EntityHierarchyRecord();
                        r.setKind(item.kind().name());
                        r.setId(id);
                        r.setAncestorId(item.parentId().orElse(null));
                        r.setLevel(item.ancestorLevel());
                        r.setDescendantLevel(item.descendantLevel());
                        return r;
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Cannot convert an item without an id to a hierarchy record"));

    public static final RecordMapper<Record, EntityHierarchyItem> TO_DOMAIN_MAPPER = record -> {
        EntityHierarchyRecord ehRecord = record.into(ENTITY_HIERARCHY);
        return ImmutableEntityHierarchyItem.builder()
                .id(ehRecord.getId())
                .kind(Enum.valueOf(EntityKind.class, ehRecord.getKind()))
                .parentId(ehRecord.getAncestorId())
                .ancestorLevel(ehRecord.getLevel())
                .descendantLevel(ehRecord.getDescendantLevel())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public EntityHierarchyDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    /**
     * Replaces an entity hierarch with a given set of hierarchy items.
     * Partial replacements are possible using the deletion filter.
     *
     * A common use of a partial replacement is when rebuilding
     * for a single measurable category.
     *
     * @param kind  then entity kind of the hierarchy to create
     * @param hierarchyItems  the items making up the hierarchy
     * @param deleteFilter  Any additional deletion restrictions
     * @return number of hierarchy records created
     */
    public int replaceHierarchy(EntityKind kind,
                                List<EntityHierarchyItem> hierarchyItems,
                                Condition deleteFilter) {
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(hierarchyItems, "hierarchyItems cannot be null");

        List<EntityHierarchyRecord> records = map(hierarchyItems, ITEM_TO_RECORD_MAPPER);

        LOG.info("Replacing hierarchy items for kind: {}, inserting new record (#{})", kind, hierarchyItems.size());
        return dsl.transactionResult(configuration -> {
            DSLContext txDsl = DSL.using(configuration);
            txDsl.deleteFrom(ENTITY_HIERARCHY)
                    .where(ENTITY_HIERARCHY.KIND.eq(kind.name()))
                    .and(deleteFilter)
                    .execute();
            return txDsl
                    .batchInsert(records)
                    .execute()
                    .length;
        });
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
                .fetch(JooqUtilities.TO_STRING_TALLY);
    }


    public List<EntityHierarchyItem> findDesendents(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl
                .select(ENTITY_HIERARCHY.fields())
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(ref.kind().name()))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }

    public List<EntityHierarchyItem> fetchHierarchyForKind(EntityKind kind) {
        return dsl
                .select(eh.fields())
                .from(eh)
                .where(eh.KIND.eq(kind.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
