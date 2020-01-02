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

package com.khartec.waltz.data.tag;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.tag.ImmutableTag;
import com.khartec.waltz.model.tag.Tag;
import com.khartec.waltz.schema.tables.records.TagRecord;
import com.khartec.waltz.schema.tables.records.TagUsageRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.Tag.TAG;
import static com.khartec.waltz.schema.tables.TagUsage.TAG_USAGE;


@Repository
public class TagDao {

    private static final Condition TAG_USAGE_JOIN_CONDITION = TAG.ID.eq(TAG_USAGE.TAG_ID);

    private final DSLContext dsl;

    @Autowired
    public TagDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Tag> findTagsForEntityReference(EntityReference ref) {
        return dsl.select(TAG.fields())
                .from(TAG)
                .join(TAG_USAGE)
                .on(TAG_USAGE_JOIN_CONDITION)
                .where(TAG_USAGE.ENTITY_ID.eq(ref.id()))
                .and(TAG_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .orderBy(TAG.NAME.asc())
                .fetch(TO_TAG_DOMAIN_MAPPER);
    }


    public List<Tag> findTagsForEntityKind(EntityKind entityKind) {
        Table<Record2<Long, Integer>> tagUsage =
                DSL.select(TAG_USAGE.TAG_ID.as("tagId"),
                        DSL.count(TAG_USAGE.ENTITY_ID).as("usageCount"))
                .from(TAG_USAGE)
                .groupBy(TAG_USAGE.TAG_ID)
                .asTable();

        return dsl
                .select(TAG.fields())
                .from(TAG)
                .join(tagUsage)
                .on(tagUsage.field("tagId", TAG.ID.getDataType()).eq(TAG.ID))
                .where(TAG.TARGET_KIND.eq(entityKind.name()))
                .orderBy(tagUsage.field("usageCount").desc())
                .fetch(TO_TAG_DOMAIN_MAPPER);
    }


    public Tag getTagByNameAndTargetKind(EntityKind entityKind, String tagName) {
        return dsl
                .select(TAG.fields())
                .from(TAG)
                .where(TAG.TARGET_KIND.eq(entityKind.name()))
                .and(TAG.NAME.in(tagName))
                .orderBy(TAG.NAME.asc())
                .fetchOne(TO_TAG_DOMAIN_MAPPER);
    }


    public void removeTagUsage(EntityReference ref, String tagToRemove) {
        SelectConditionStep<Record1<Long>> tagIdsByName = DSL
                .select(TAG.ID)
                .from(TAG)
                .where(TAG.NAME.in(tagToRemove))
                .and(TAG.TARGET_KIND.eq(ref.kind().name()));

        dsl.delete(TAG_USAGE)
                .where(TAG_USAGE.ENTITY_ID.eq(ref.id()))
                .and(TAG_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .and(TAG_USAGE.TAG_ID.in(tagIdsByName))
                .execute();

        // Remove unused tags
        SelectJoinStep<Record1<Long>> usedTagIds = DSL
                .selectDistinct(TAG_USAGE.TAG_ID)
                .from(TAG_USAGE);

        dsl.delete(TAG)
                .where(TAG.ID.notIn(usedTagIds))
                .and(TAG.NAME.in(tagToRemove))
                .execute();
    }


    public Long createTag(EntityKind targetKind, String tag) {
        return dsl.insertInto(TAG)
                .set(TAG.NAME, tag)
                .set(TAG.TARGET_KIND, targetKind.name())
                .returning(TAG.ID)
                .fetchOne()
                .getId();
    }


    public void createTagUsage(EntityReference ref, String username, Long tagId) {
        TagUsageRecord r = new TagUsageRecord();
        r.setTagId(tagId);
        r.setEntityId(ref.id());
        r.setEntityKind(ref.kind().name());
        r.setCreatedBy(username);
        r.setProvenance("waltz");
        dsl.executeInsert(r);
    }


    public Tag getById(long id) {
        return dsl
                .select(TAG.fields())
                .from(TAG)
                .where(TAG.ID.eq(id))
                .fetchOne(TO_TAG_DOMAIN_MAPPER);
    }


    private static final RecordMapper<Record, Tag> TO_TAG_DOMAIN_MAPPER = r -> {
        TagRecord record = r.into(TagRecord.class);

        return ImmutableTag.builder()
                .id(record.getId())
                .name(record.getName())
                .targetKind(EntityKind.valueOf(record.getTargetKind()))
                .build();
    };
}
