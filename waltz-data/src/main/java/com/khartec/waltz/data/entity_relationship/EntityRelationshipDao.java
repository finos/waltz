package com.khartec.waltz.data.entity_relationship;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import com.khartec.waltz.model.entiy_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.schema.tables.records.EntityRelationshipRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;


@Repository
public class EntityRelationshipDao {

    private static final RecordMapper<Record, EntityRelationship> TO_DOMAIN_MAPPER = r -> {
        EntityRelationshipRecord record = r.into(ENTITY_RELATIONSHIP);
        return ImmutableEntityRelationship.builder()
                .a(ImmutableEntityReference.builder()
                        .id(record.getIdA())
                        .kind(EntityKind.valueOf(record.getKindA()))
                        .build())
                .b(ImmutableEntityReference.builder()
                        .id(record.getIdB())
                        .kind(EntityKind.valueOf(record.getKindB()))
                        .build())
                .provenance(record.getProvenance())
                .relationship(RelationshipKind.valueOf(record.getRelationship()))
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

        return dsl.select(ENTITY_RELATIONSHIP.fields())
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()).and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name())))
                .or(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()).and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name())))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int save(EntityRelationship entityRelationship) {
        checkNotNull(entityRelationship, "entityRelationship cannot be null");

        return ! exists(entityRelationship)
                ? dsl.executeInsert(TO_RECORD_MAPPER.apply(entityRelationship))
                : 0;
    }


    public int remove(EntityRelationship entityRelationship) {
        checkNotNull(entityRelationship, "entityRelationship cannot be null");

        return exists(entityRelationship)
                ? dsl.executeDelete(TO_RECORD_MAPPER.apply(entityRelationship))
                : 0;
    }


    private boolean exists(EntityRelationship rel) {
        int count = dsl.fetchCount(DSL.select().from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.ID_A.eq(rel.a().id())
                        .and(ENTITY_RELATIONSHIP.KIND_A.eq(rel.a().kind().name())))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(rel.b().id())
                        .and(ENTITY_RELATIONSHIP.KIND_B.eq(rel.b().kind().name()))));

        return count > 0;
    }


}
