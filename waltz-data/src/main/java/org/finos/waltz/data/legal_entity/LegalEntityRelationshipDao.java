package org.finos.waltz.data.legal_entity;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;

@Repository
public class LegalEntityRelationshipDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_ID,
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND,
                    newArrayList(EntityKind.APPLICATION))
            .as("entity_name");
    private final DSLContext dsl;
    private static final RecordMapper<Record, LegalEntityRelationship> TO_DOMAIN_MAPPER = r -> {
        LegalEntityRelationshipRecord record = r.into(LEGAL_ENTITY_RELATIONSHIP);

        EntityReference targetEntityReference = mkRef(EntityKind.valueOf(record.getTargetKind()), record.getTargetId(), r.get(ENTITY_NAME_FIELD));
        EntityReference legalEntityReference = mkRef(EntityKind.LEGAL_ENTITY, record.getLegalEntityId(), r.get(LEGAL_ENTITY.NAME));

        return ImmutableLegalEntityRelationship.builder()
                .id(record.getId())
                .legalEntityReference(legalEntityReference)
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
                .select(LEGAL_ENTITY_RELATIONSHIP.fields())
                .select(LEGAL_ENTITY.NAME)
                .select(ENTITY_NAME_FIELD)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .innerJoin(LEGAL_ENTITY).on(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(LEGAL_ENTITY.ID))
                .where(condition)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

    public Set<LegalEntityRelationship> findByEntityReference(EntityReference ref) {
        Condition targetRefCondition = LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.eq(ref.id())
                .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.eq(ref.kind().name()));
        return findByCondition(targetRefCondition);
    }
}
