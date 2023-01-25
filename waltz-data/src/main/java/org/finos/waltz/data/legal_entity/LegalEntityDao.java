package org.finos.waltz.data.legal_entity;

import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntity;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.finos.waltz.schema.tables.records.LegalEntityRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY;

@Repository
public class LegalEntityDao {

    private final DSLContext dsl;

    public static final RecordMapper<Record, LegalEntity> TO_DOMAIN_MAPPER = r -> {
        LegalEntityRecord record = r.into(LEGAL_ENTITY);

        return ImmutableLegalEntity.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .entityLifecycleStatus(EntityLifecycleStatus.valueOf(record.getEntityLifecycleStatus()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    @Autowired
    public LegalEntityDao(DSLContext dsl) {
        this.dsl = dsl;
    }

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


    public Set<LegalEntity> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(LEGAL_ENTITY.fields())
                .from(LEGAL_ENTITY)
                .where(LEGAL_ENTITY.ID.in(selector))
                .fetchSet(TO_DOMAIN_MAPPER);
    }
}
