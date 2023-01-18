package org.finos.waltz.data.legal_entity;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY_RELATIONSHIP_KIND;

@Repository
public class LegalEntityRelationshipDao {

    private final DSLContext dsl;
    private static final RecordMapper<Record, LegalEntityRelationship> TO_DOMAIN_MAPPER = r -> {
        LegalEntityRelationshipRecord record = r.into(LEGAL_ENTITY_RELATIONSHIP);

        EntityReference targetEntityReference = mkRef(EntityKind.valueOf(record.getTargetKind()), record.getTargetId());

        return ImmutableLegalEntityRelationship.builder()
                .id(record.getId())
                .legalEntityId(record.getLegalEntityId())
                .relationshipKindId(record.getRelationshipKindId())
                .description(record.getDescription())
                .targetEntityReference(targetEntityReference)
                .externalId(Optional.ofNullable(record.getExternalId()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .isReadOnly(record.getIsReadonly())
                .build();
    };

    @Autowired
    public LegalEntityRelationshipDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<LegalEntityRelationship> findByLegalEntityId(Long legalEntityId) {
        Condition legalEntityCondition = LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(legalEntityId);
        return findByCondition(legalEntityCondition);
    }

    private Set<LegalEntityRelationship> findByCondition(Condition condition) {
        return dsl
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.fields())
                .from(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .where(condition)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

}
