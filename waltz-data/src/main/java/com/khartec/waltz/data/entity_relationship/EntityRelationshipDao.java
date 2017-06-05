/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.entity_relationship;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_relationship.*;
import com.khartec.waltz.schema.tables.records.EntityRelationshipRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.EntityNameUtilities.mkEntityNameField;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;


@Repository
public class EntityRelationshipDao {


    private static List<EntityKind> POSSIBLE_ENTITIES = newArrayList(
            EntityKind.APPLICATION,
            EntityKind.ACTOR,
            EntityKind.MEASURABLE,
            EntityKind.CHANGE_INITIATIVE);


    private static Field<String> NAME_A = mkEntityNameField(
            ENTITY_RELATIONSHIP.ID_A,
            ENTITY_RELATIONSHIP.KIND_A,
            POSSIBLE_ENTITIES);


    private static Field<String> NAME_B = mkEntityNameField(
            ENTITY_RELATIONSHIP.ID_B,
            ENTITY_RELATIONSHIP.KIND_B,
            POSSIBLE_ENTITIES);


    private static final RecordMapper<Record, EntityRelationship> TO_DOMAIN_MAPPER = r -> {
        EntityRelationshipRecord record = r.into(ENTITY_RELATIONSHIP);
        return ImmutableEntityRelationship.builder()
                .a(mkRef(
                        EntityKind.valueOf(record.getKindA()),
                        record.getIdA(),
                        r.get(NAME_A)))
                .b(mkRef(
                        EntityKind.valueOf(record.getKindB()),
                        record.getIdB(),
                        r.get(NAME_B)))
                .provenance(record.getProvenance())
                .relationship(RelationshipKind.valueOf(record.getRelationship()))
                .description(record.getDescription())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .build();
    };


    private static final Function<EntityRelationship, EntityRelationshipRecord> TO_RECORD_MAPPER = rel -> {
        EntityRelationshipRecord record = new EntityRelationshipRecord();
        record.setRelationship(rel.relationship().name());
        record.setIdA(rel.a().id());
        record.setKindA(rel.a().kind().name());
        record.setIdB(rel.b().id());
        record.setKindB(rel.b().kind().name());
        record.setProvenance(rel.provenance());
        record.setDescription(rel.description());
        record.setLastUpdatedBy(rel.lastUpdatedBy());
        record.setLastUpdatedAt(Timestamp.valueOf(rel.lastUpdatedAt()));
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public EntityRelationshipDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<EntityRelationship> findRelationshipsInvolving(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl
                .select(ENTITY_RELATIONSHIP.fields())
                .select(NAME_A, NAME_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()).and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name())))
                .or(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()).and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name())))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public int save(EntityRelationship entityRelationship) {
        checkNotNull(entityRelationship, "entityRelationship cannot be null");

        return ! exists(entityRelationship.toKey())
                ? dsl.executeInsert(TO_RECORD_MAPPER.apply(entityRelationship))
                : 0;
    }


    @Deprecated
    public boolean remove(EntityRelationship entityRelationship) {
        checkNotNull(entityRelationship, "entityRelationship cannot be null");
        EntityRelationshipKey key = entityRelationship.toKey();
        return remove(key);
    }



    public boolean create(EntityRelationship relationship) {
        return dsl.executeInsert(TO_RECORD_MAPPER.apply(relationship)) == 1;
    }


    public boolean remove(EntityRelationshipKey key) {
        checkNotNull(key, "key cannot be null");

        return dsl
                .deleteFrom(ENTITY_RELATIONSHIP)
                .where(keyMatches(key))
                .execute() == 1;
    }


    public boolean update(EntityRelationshipKey key,
                          UpdateEntityRelationshipParams params,
                          String username) {
        return dsl.update(ENTITY_RELATIONSHIP)
                .set(ENTITY_RELATIONSHIP.RELATIONSHIP, params.relationshipKind().name())
                .set(ENTITY_RELATIONSHIP.DESCRIPTION, params.description())
                .set(ENTITY_RELATIONSHIP.LAST_UPDATED_BY, username)
                .set(ENTITY_RELATIONSHIP.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .where(keyMatches(key))
                .execute() == 1;

    }


    // --- HELPERS ---

    private boolean exists(EntityRelationshipKey key) {

        int count = dsl.fetchCount(
                DSL.select()
                        .from(ENTITY_RELATIONSHIP)
                        .where(keyMatches(key)));

        return count > 0;
    }


    private Condition keyMatches(EntityRelationshipKey key) {
        return ENTITY_RELATIONSHIP.ID_A.eq(key.a().id())
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(key.a().kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(key.b().id())
                        .and(ENTITY_RELATIONSHIP.KIND_B.eq(key.b().kind().name())))
                .and(ENTITY_RELATIONSHIP.RELATIONSHIP.eq(key.relationshipKind().name()));
    }


}
