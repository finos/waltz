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

package com.khartec.waltz.data.tag;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.tag.ImmutableTag;
import com.khartec.waltz.model.tag.ImmutableTagUsage;
import com.khartec.waltz.model.tag.Tag;
import com.khartec.waltz.model.tag.TagUsage;
import com.khartec.waltz.schema.tables.records.TagRecord;
import com.khartec.waltz.schema.tables.records.TagUsageRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Tag.TAG;
import static com.khartec.waltz.schema.tables.TagUsage.TAG_USAGE;
import static java.util.Collections.emptyList;


@Repository
public class TagDao {

    private static final Condition TAG_USAGE_JOIN_CONDITION = TAG.ID.eq(TAG_USAGE.TAG_ID);

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            TAG_USAGE.ENTITY_ID,
            TAG_USAGE.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ORG_UNIT));

    private final DSLContext dsl;


    @Autowired
    public TagDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    @Deprecated
    public List<Tag> findAllTags() {
        return dsl.selectDistinct(TAG.fields())
                .from(TAG)
                .orderBy(TAG.NAME.asc())
                .fetch(TO_TAG_DOMAIN_MAPPER);
    }

    @Deprecated
    public List<EntityReference> findByTag(String tag) {
        if (isEmpty(tag)) { return emptyList(); }

        return dsl.select(TAG_USAGE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(TAG)
                .join(TAG_USAGE)
                .on(TAG_USAGE_JOIN_CONDITION)
                .where(TAG.NAME.equalIgnoreCase(tag))
                .orderBy(TAG_USAGE.ENTITY_ID)
                .fetch(r -> mkRef(
                        EntityKind.valueOf(r.getValue(TAG_USAGE.ENTITY_KIND)),
                        r.getValue(TAG_USAGE.ENTITY_ID),
                        r.getValue(ENTITY_NAME_FIELD)));
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
        return dsl
                .select(TAG.fields())
                .from(TAG)
                .where(TAG.TARGET_KIND.eq(entityKind.name()))
                .orderBy(TAG.NAME.asc())
                .fetch(TO_TAG_DOMAIN_MAPPER);
    }

    public Tag findTagByNameAndTargetKind(EntityKind entityKind, String tagName) {
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
                .and(TAG.NAME.in(tagToRemove));
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


    private static final RecordMapper<Record, Tag> TO_TAG_DOMAIN_MAPPER = r -> {
        TagRecord record = r.into(TagRecord.class);

        return ImmutableTag.builder()
                .id(record.getId())
                .name(record.getName())
                .targetKind(EntityKind.valueOf(record.getTargetKind()))
                .build();
    };

    private static final RecordMapper<Record, TagUsage> TO_TAG_USAGE_DOMAIN_MAPPER = r -> {
        TagUsageRecord record = r.into(TagUsageRecord.class);

        return ImmutableTagUsage.builder()
                .tagId(record.getTagId())
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .createdBy(record.getCreatedBy())
                .provenance(record.getProvenance())
                .entityReference(mkRef(
                        EntityKind.valueOf(r.getValue(TAG_USAGE.ENTITY_KIND)),
                        r.getValue(TAG_USAGE.ENTITY_ID),
                        r.getValue(ENTITY_NAME_FIELD)))
                .build();
    };

    public Tag getById(long id) {
        return dsl
                .select(TAG.fields())
                .from(TAG)
                .where(TAG.ID.eq(id))
                .fetchOne(TO_TAG_DOMAIN_MAPPER);
    }

    public List<TagUsage> getTagUsageByTagId(long tagId) {
        return dsl
                .select(TAG_USAGE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(TAG)
                .join(TAG_USAGE)
                .on(TAG_USAGE_JOIN_CONDITION)
                .where(TAG.ID.eq(tagId))
                .fetch(TO_TAG_USAGE_DOMAIN_MAPPER);
    }
}
