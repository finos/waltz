package com.khartec.waltz.data.entity_relationship;

import com.khartec.waltz.common.Checks;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

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


    private final DSLContext dsl;

    @Autowired
    public EntityRelationshipDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<EntityRelationship> findRelationshipsInvolving(EntityReference ref) {
        Checks.checkNotNull(ref, "ref cannot be null");

        return dsl.select(ENTITY_RELATIONSHIP.fields())
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()).and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name())))
                .or(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()).and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name())))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
