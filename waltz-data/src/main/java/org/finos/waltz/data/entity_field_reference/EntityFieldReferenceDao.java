package org.finos.waltz.data.entity_field_reference;


import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.finos.waltz.model.entity_field_reference.ImmutableEntityFieldReference;
import org.finos.waltz.schema.tables.records.EntityFieldReferenceRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.ENTITY_FIELD_REFERENCE;

@Repository
public class EntityFieldReferenceDao {

    private final DSLContext dsl;

    @Autowired
    public EntityFieldReferenceDao(DSLContext dsl) {
        checkNotNull(dsl, " cannot be null");
        this.dsl = dsl;
    }

    private static final RecordMapper<Record, EntityFieldReference> TO_DOMAIN_MAPPER = r -> {
        EntityFieldReferenceRecord record = r.into(ENTITY_FIELD_REFERENCE);
        return ImmutableEntityFieldReference.builder()
                .id(record.getId())
                .displayName(record.getDisplayName())
                .description(record.getDescription())
                .fieldName(record.getFieldName())
                .entityKind(EntityKind.valueOf(record.getEntityKind()))
                .build();
    };


    public Set<EntityFieldReference> findAll() {
        return dsl
                .select(ENTITY_FIELD_REFERENCE.fields())
                .from(ENTITY_FIELD_REFERENCE)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

}
