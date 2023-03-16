package org.finos.waltz.data.legal_entity;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationship;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationshipAssessmentInfo;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipAssessmentInfo;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.ObjectUtilities.firstNotNull;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.assessment_definition.AssessmentVisibility.PRIMARY;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class LegalEntityRelationshipDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_ID,
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND,
                    newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    private static final Field<String> ENTITY_EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_ID,
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND,
                    newArrayList(EntityKind.APPLICATION))
            .as("entity_ext_id");
    private final DSLContext dsl;
    private static final RecordMapper<Record, LegalEntityRelationship> TO_DOMAIN_MAPPER = r -> {
        LegalEntityRelationshipRecord record = r.into(LEGAL_ENTITY_RELATIONSHIP);

        EntityReference targetEntityReference = mkRef(EntityKind.valueOf(record.getTargetKind()), record.getTargetId(), r.get(ENTITY_NAME_FIELD), null, r.get(ENTITY_EXTERNAL_ID_FIELD));
        EntityReference legalEntityReference = mkRef(EntityKind.LEGAL_ENTITY, record.getLegalEntityId(), r.get(LEGAL_ENTITY.NAME), null, r.get(LEGAL_ENTITY.EXTERNAL_ID));

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

    private static final Function<LegalEntityRelationship, LegalEntityRelationshipRecord> TO_RECORD_MAPPER = d -> {

        LegalEntityRelationshipRecord r = new LegalEntityRelationshipRecord();

        d.id().ifPresent(r::setId);
        r.setLegalEntityId(d.legalEntityReference().id());
        r.setTargetId(d.targetEntityReference().id());
        r.setTargetKind(d.targetEntityReference().kind().name());
        r.setRelationshipKindId(d.relationshipKindId());
        r.setExternalId(d.externalId().orElse(null));
        r.setDescription(d.description());
        r.setLastUpdatedAt(Timestamp.valueOf(d.lastUpdatedAt()));
        r.setLastUpdatedBy(d.lastUpdatedBy());
        r.setProvenance(d.provenance());
        r.setIsReadonly(d.isReadOnly());

        r.changed(LEGAL_ENTITY_RELATIONSHIP.ID, false);

        return r;
    };

    @Autowired
    public LegalEntityRelationshipDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public LegalEntityRelationship getById(Long id) {
        return dsl
                .select(LEGAL_ENTITY_RELATIONSHIP.fields())
                .select(LEGAL_ENTITY.NAME)
                .select(LEGAL_ENTITY.EXTERNAL_ID)
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_EXTERNAL_ID_FIELD)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .innerJoin(LEGAL_ENTITY).on(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(LEGAL_ENTITY.ID))
                .where(LEGAL_ENTITY_RELATIONSHIP.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public Set<LegalEntityRelationship> findByLegalEntityId(Long legalEntityId) {
        Condition legalEntityCondition = LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(legalEntityId);
        return findByCondition(null, legalEntityCondition);
    }

    private Set<LegalEntityRelationship> findByCondition(DSLContext tx, Condition condition) {

        DSLContext dslContext = firstNotNull(tx, dsl);

        return dslContext
                .select(LEGAL_ENTITY_RELATIONSHIP.fields())
                .select(LEGAL_ENTITY.NAME)
                .select(LEGAL_ENTITY.EXTERNAL_ID)
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_EXTERNAL_ID_FIELD)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .innerJoin(LEGAL_ENTITY).on(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(LEGAL_ENTITY.ID))
                .where(condition)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

    public Set<LegalEntityRelationship> findByEntityReference(EntityReference ref) {
        Condition targetRefCondition = LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.eq(ref.id())
                .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.eq(ref.kind().name()));
        return findByCondition(null, targetRefCondition);
    }

    public Set<LegalEntityRelationship> findByRelationshipKind(DSLContext tx, long relKindId) {
        Condition relationshipKindCondition = LEGAL_ENTITY_RELATIONSHIP.RELATIONSHIP_KIND_ID.eq(relKindId);
        return findByCondition(tx, relationshipKindCondition);
    }

    public int bulkAdd(DSLContext tx, Set<LegalEntityRelationship> relationships) {

        Set<LegalEntityRelationshipRecord> recordsToInsert = map(relationships, TO_RECORD_MAPPER);

        int[] insertedRcs = tx.batchInsert(recordsToInsert).execute();

        return summarizeResults(insertedRcs);
    }

    public int bulkUpdate(DSLContext tx, Set<LegalEntityRelationship> relationships) {

        int[] updatedRcs = relationships
                .stream()
                .map(TO_RECORD_MAPPER)
                .peek(r -> r.changed(LEGAL_ENTITY_RELATIONSHIP.DESCRIPTION, notEmpty(r.getDescription()))) // prevent overwriting comment
                .collect(collectingAndThen(
                        toSet(),
                        tx::batchUpdate))
                .execute();

        return summarizeResults(updatedRcs);
    }

    public int bulkRemove(DSLContext tx, Set<LegalEntityRelationship> relationships) {

        int[] deletedRcs = relationships
                .stream()
                .map(d -> tx
                        .deleteFrom(LEGAL_ENTITY_RELATIONSHIP)
                        .where(LEGAL_ENTITY_RELATIONSHIP.ID.eq(d.id().get())))
                .collect(collectingAndThen(toSet(), tx::batch))
                .execute();

        return summarizeResults(deletedRcs);
    }

    public Set<LegalEntityRelationshipAssessmentInfo> getViewAssessmentsByRelKind(long relKindId) {

        org.finos.waltz.schema.tables.LegalEntityRelationship ler = LEGAL_ENTITY_RELATIONSHIP.as("ler");
        org.finos.waltz.schema.tables.LegalEntity le = LEGAL_ENTITY.as("le");
        org.finos.waltz.schema.tables.AssessmentDefinition ad = ASSESSMENT_DEFINITION.as("ad");
        org.finos.waltz.schema.tables.AssessmentRating ar = ASSESSMENT_RATING.as("ar");
        org.finos.waltz.schema.tables.RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");

        Field<String> defnName = ad.NAME.as("defnName");

        return dsl
                .select(ler.ID)
                .select(ad.ID,
                        defnName)
                .select(rsi.fields())
                .from(ler)
                .innerJoin(le).on(ler.LEGAL_ENTITY_ID.eq(le.ID))
                .innerJoin(ar).on(ler.ID.eq(ar.ENTITY_ID).and(ar.ENTITY_KIND.eq(EntityKind.LEGAL_ENTITY_RELATIONSHIP.name())))
                .innerJoin(ad).on(ar.ASSESSMENT_DEFINITION_ID.eq(ad.ID))
                .innerJoin(rsi).on(ar.RATING_ID.eq(rsi.ID))
                .where(ler.RELATIONSHIP_KIND_ID.eq(relKindId))
                .and(ad.VISIBILITY.eq(PRIMARY.name()))
                .fetchSet(r -> ImmutableLegalEntityRelationshipAssessmentInfo
                        .builder()
                        .relationshipId(r.get(ler.ID))
                        .definitionRef(mkRef(EntityKind.ASSESSMENT_DEFINITION, r.get(ad.ID), r.get(defnName)))
                        .ratingItem(RatingSchemeDAO.TO_ITEM_MAPPER.map(r))
                        .build());
    }

}
