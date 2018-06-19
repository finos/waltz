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

package com.khartec.waltz.data.entity_tag;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.schema.tables.records.EntityTagRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.schema.tables.EntityTag.ENTITY_TAG;
import static java.util.Collections.emptyList;


@Repository
public class EntityTagDao {

    private static final Logger LOG = LoggerFactory.getLogger(EntityTagDao.class);

    private final DSLContext dsl;


    @Autowired
    public EntityTagDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<String> findAllTags() {
        return dsl.selectDistinct(ENTITY_TAG.TAG)
                .from(ENTITY_TAG)
                .orderBy(ENTITY_TAG.TAG.asc())
                .fetch(ENTITY_TAG.TAG);
    }


    public List<String> findTagsForEntityReference(EntityReference ref) {
        return dsl.select(ENTITY_TAG.TAG)
                .from(ENTITY_TAG)
                .where(ENTITY_TAG.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_TAG.ENTITY_KIND.eq(ref.kind().name()))
                .orderBy(ENTITY_TAG.TAG.asc())
                .fetch(ENTITY_TAG.TAG);
    }


    public List<EntityReference> findByTag(String tag) {
        if (isEmpty(tag)) { return emptyList(); }

        Field<String> entityNameField = InlineSelectFieldFactory.mkNameField(
                ENTITY_TAG.ENTITY_ID,
                ENTITY_TAG.ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ORG_UNIT));

        return dsl.select(ENTITY_TAG.ENTITY_ID, ENTITY_TAG.ENTITY_KIND, entityNameField)
                .from(ENTITY_TAG)
                .where(ENTITY_TAG.TAG.equalIgnoreCase(tag))
                .orderBy(entityNameField)
                .fetch(r -> EntityReference.mkRef(
                        EntityKind.valueOf(r.getValue(ENTITY_TAG.ENTITY_KIND)),
                        r.getValue(ENTITY_TAG.ENTITY_ID),
                        r.getValue(entityNameField)));
    }


    public int[] updateTags(EntityReference ref, Collection<String> tags, String username) {
        LOG.info("Updating tags for entity ref: {}, tags: {} ", ref, tags);

        dsl.delete(ENTITY_TAG)
                .where(ENTITY_TAG.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_TAG.ENTITY_KIND.eq(ref.kind().name()))
                .execute();

        List<EntityTagRecord> records = tags
                .stream()
                .map(t -> {
                    EntityTagRecord record = new EntityTagRecord();
                    record.setEntityId(ref.id());
                    record.setEntityKind(ref.kind().name());
                    record.setTag(t);
                    record.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
                    record.setLastUpdatedBy(username);
                    record.setProvenance("waltz");
                    return record;
                })
                .collect(Collectors.toList());

        return dsl.batchInsert(records)
                .execute();
    }
}
