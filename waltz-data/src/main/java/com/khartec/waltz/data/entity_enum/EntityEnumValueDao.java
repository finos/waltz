package com.khartec.waltz.data.entity_enum;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_enum.EntityEnumValue;
import com.khartec.waltz.model.entity_enum.ImmutableEntityEnumValue;
import com.khartec.waltz.schema.tables.records.EntityEnumValueRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.EntityEnumValue.ENTITY_ENUM_VALUE;

@Repository
public class EntityEnumValueDao {

    public static final RecordMapper<? super Record, EntityEnumValue> TO_DOMAIN_MAPPER = r -> {
        EntityEnumValueRecord record = r.into(ENTITY_ENUM_VALUE);

        return ImmutableEntityEnumValue.builder()
                .definitionId(record.getDefinitionId())
                .entityReference(mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .enumValueKey(record.getEnumValueKey())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityEnumValueDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityEnumValue> findByEntity(EntityReference ref) {
        return dsl.selectFrom(ENTITY_ENUM_VALUE)
                .where(ENTITY_ENUM_VALUE.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_ENUM_VALUE.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
