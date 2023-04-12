package org.finos.waltz.data.legal_entity;

import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelKindStat;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationshipKind;
import org.finos.waltz.model.legal_entity.LegalEntityRelKindStat;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.schema.tables.LegalEntityRelationship;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipKindRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class LegalEntityRelationshipKindDao {

    private static final LegalEntityRelationship distinctLERels = LEGAL_ENTITY_RELATIONSHIP.as("leRels");
    private static final LegalEntityRelationship distinctTargetEntityRels = LEGAL_ENTITY_RELATIONSHIP.as("teRels");
    private static final LegalEntityRelationship relationships = LEGAL_ENTITY_RELATIONSHIP.as("rels");
    private static final Field<Integer> legalEntityCount = DSL.countDistinct(distinctLERels.LEGAL_ENTITY_ID).as("leCount");
    private static final Field<Integer> targetEntityCount = DSL.countDistinct(distinctTargetEntityRels.TARGET_ID).as("teCount");
    private static final Field<Integer> relCount = DSL.countDistinct(relationships.ID).as("relCount");


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

    public Set<LegalEntityRelKindStat> findUsageStats() {

        SelectHavingStep<Record2<Long, Integer>> legalEntityCountsByRelKind = mkCountSelect(
                distinctLERels,
                legalEntityCount,
                DSL.trueCondition());

        SelectHavingStep<Record2<Long, Integer>> targetEntityCountsByRelKind = mkCountSelect(
                distinctTargetEntityRels,
                targetEntityCount,
                DSL.trueCondition());

        SelectHavingStep<Record2<Long, Integer>> relationshipCountsByRelKind = mkCountSelect(
                relationships,
                relCount,
                DSL.trueCondition());

        Field<Integer> leCountField = DSL.coalesce(legalEntityCountsByRelKind.field(legalEntityCount), 0);
        Field<Integer> teCountField = DSL.coalesce(targetEntityCountsByRelKind.field(targetEntityCount), 0);
        Field<Integer> relCountField = DSL.coalesce(relationshipCountsByRelKind.field(relCount), 0);

        SelectOnConditionStep<Record> qry = dsl
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.ID)
                .select(leCountField)
                .select(teCountField)
                .select(relCountField)
                .from(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .leftJoin(legalEntityCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(legalEntityCountsByRelKind.field(distinctLERels.RELATIONSHIP_KIND_ID)))
                .leftJoin(targetEntityCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(targetEntityCountsByRelKind.field(distinctTargetEntityRels.RELATIONSHIP_KIND_ID)))
                .leftJoin(relationshipCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(relationshipCountsByRelKind.field(relationships.RELATIONSHIP_KIND_ID)));

        return qry
                .fetchSet(r -> ImmutableLegalEntityRelKindStat.builder()
                        .relKindId(r.get(LEGAL_ENTITY_RELATIONSHIP_KIND.ID))
                        .legalEntityCount(r.get(leCountField))
                        .targetEntityCount(r.get(teCountField))
                        .relationshipCount(r.get(relCountField))
                        .build());
    }

    public LegalEntityRelKindStat getUsageStatsByKindAndSelector(Long relKindId,
                                                                 Select<Record1<Long>> relSelector) {

        SelectHavingStep<Record2<Long, Integer>> legalEntityCountsByRelKind = mkCountSelect(
                distinctLERels,
                legalEntityCount,
                mkCondition(distinctLERels, relKindId, relSelector));

        SelectHavingStep<Record2<Long, Integer>> targetEntityCountsByRelKind = mkCountSelect(
                distinctTargetEntityRels,
                targetEntityCount,
                mkCondition(distinctTargetEntityRels, relKindId, relSelector));

        SelectHavingStep<Record2<Long, Integer>> relationshipCountsByRelKind = mkCountSelect(relationships, relCount, mkCondition(relationships, relKindId, relSelector));

        Field<Integer> leCountField = DSL.coalesce(legalEntityCountsByRelKind.field(legalEntityCount), 0);
        Field<Integer> teCountField = DSL.coalesce(targetEntityCountsByRelKind.field(targetEntityCount), 0);
        Field<Integer> relCountField = DSL.coalesce(relationshipCountsByRelKind.field(relCount), 0);

        SelectConditionStep<Record> qry = dsl
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.ID)
                .select(leCountField)
                .select(teCountField)
                .select(relCountField)
                .from(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .leftJoin(legalEntityCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(legalEntityCountsByRelKind.field(distinctLERels.RELATIONSHIP_KIND_ID)))
                .leftJoin(targetEntityCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(targetEntityCountsByRelKind.field(distinctTargetEntityRels.RELATIONSHIP_KIND_ID)))
                .leftJoin(relationshipCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(relationshipCountsByRelKind.field(relationships.RELATIONSHIP_KIND_ID)))
                .where(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(relKindId));

        return qry
                .fetchOne(r -> ImmutableLegalEntityRelKindStat.builder()
                        .relKindId(relKindId)
                        .legalEntityCount(r.get(leCountField))
                        .targetEntityCount(r.get(teCountField))
                        .relationshipCount(r.get(relCountField))
                        .build());
    }

    private Condition mkCondition(LegalEntityRelationship lerTable,
                                  Long relKindId,
                                  Select<Record1<Long>> relSelector) {
        return lerTable.RELATIONSHIP_KIND_ID.eq(relKindId)
                .and(lerTable.ID.in(relSelector));
    }

    private SelectHavingStep<Record2<Long, Integer>> mkCountSelect(LegalEntityRelationship lerTable,
                                                                   Field<Integer> countField,
                                                                   Condition condition) {
        return DSL
                .select(lerTable.RELATIONSHIP_KIND_ID,
                        countField)
                .from(lerTable)
                .where(condition)
                .groupBy(lerTable.RELATIONSHIP_KIND_ID);
    }
}
