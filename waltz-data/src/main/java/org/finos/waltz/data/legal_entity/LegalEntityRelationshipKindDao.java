package org.finos.waltz.data.legal_entity;

import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationshipKind;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipKindRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY_RELATIONSHIP_KIND;

@Repository
public class LegalEntityRelationshipKindDao {

    private final DSLContext dsl;
    private static final RecordMapper<Record, LegalEntityRelationshipKind> TO_DOMAIN_MAPPER = r -> {
        LegalEntityRelationshipKindRecord record = r.into(LEGAL_ENTITY_RELATIONSHIP_KIND);

        return ImmutableLegalEntityRelationshipKind.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .targetKind(EntityKind.valueOf(record.getTargetKind()))
                .cardinality(Cardinality.valueOf(record.getCardinality()))
                .requiredRole(record.getRequiredRole())
                .externalId(record.getExternalId())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    @Autowired
    public LegalEntityRelationshipKindDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public LegalEntityRelationshipKind getById(long id) {
        return dsl
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.fields())
                .from(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .where(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<LegalEntityRelationshipKind> findAll() {
        return dsl
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.fields())
                .from(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

}
