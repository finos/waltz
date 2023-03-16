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


        LegalEntityRelationship distinctLERels = LEGAL_ENTITY_RELATIONSHIP.as("leRels");
        LegalEntityRelationship distinctTargetEntityRels = LEGAL_ENTITY_RELATIONSHIP.as("teRels");

        Field<Integer> legalEntityCount = DSL.countDistinct(distinctLERels.LEGAL_ENTITY_ID).as("leCount");

        SelectHavingStep<Record2<Long, Integer>> legalEntityCountsByRelKind = DSL
                .select(distinctLERels.RELATIONSHIP_KIND_ID,
                        legalEntityCount)
                .from(distinctLERels)
                .groupBy(distinctLERels.RELATIONSHIP_KIND_ID);

        Field<Integer> targetEntityCount = DSL.countDistinct(distinctTargetEntityRels.TARGET_ID).as("teCount");

        SelectHavingStep<Record2<Long, Integer>> targetEntityCountsByRelKind = DSL
                .select(distinctTargetEntityRels.RELATIONSHIP_KIND_ID,
                        targetEntityCount)
                .from(distinctTargetEntityRels)
                .groupBy(distinctTargetEntityRels.RELATIONSHIP_KIND_ID);

        Field<Integer> leCountField = DSL.coalesce(legalEntityCountsByRelKind.field(legalEntityCount), 0);
        Field<Integer> teCountField = DSL.coalesce(targetEntityCountsByRelKind.field(targetEntityCount), 0);

        SelectOnConditionStep<Record> qry = dsl
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.ID)
                .select(leCountField)
                .select(teCountField)
                .from(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .leftJoin(legalEntityCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(legalEntityCountsByRelKind.field(distinctLERels.RELATIONSHIP_KIND_ID)))
                .leftJoin(targetEntityCountsByRelKind)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(targetEntityCountsByRelKind.field(distinctTargetEntityRels.RELATIONSHIP_KIND_ID)));

        return qry
                .fetchSet(r -> ImmutableLegalEntityRelKindStat.builder()
                        .relKindId(r.get(LEGAL_ENTITY_RELATIONSHIP_KIND.ID))
                        .legalEntityCount(r.get(leCountField))
                        .targetEntityCount(r.get(teCountField))
                        .build());
    }
}
