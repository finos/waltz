package org.finos.waltz.data.legal_entity;

import org.finos.waltz.model.legal_entity.ImmutableLegalEntity;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.finos.waltz.schema.tables.records.LegalEntityRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;

import java.util.Set;

import static org.finos.waltz.schema.Tables.LEGAL_ENTITY;

public class LegalRelationshipKindDao {

    private final DSLContext dsl;

    public LegalRelationshipKindDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    private static final RecordMapper<Record, LegalEntity> TO_DOMAIN_MAPPER = r -> {
        LegalEntityRecord record = r.into(LEGAL_ENTITY);

        return ImmutableLegalEntity.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .entityLifecycleStatus(record.getEntityLifecycleStatus())
                .lastUpdatedAt(record.getLastUpdatedAt())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    public LegalEntity getById(long id) {
        return dsl
                .select(LEGAL_ENTITY.fields())
                .from(LEGAL_ENTITY)
                .where(LEGAL_ENTITY.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<LegalEntity> findAll() {
        return dsl
                .select(LEGAL_ENTITY.fields())
                .from(LEGAL_ENTITY)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

}
