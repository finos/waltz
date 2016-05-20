package com.khartec.waltz.data.change_initiative;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.change_initiative.ChangeInitiativeKind;
import com.khartec.waltz.model.change_initiative.ImmutableChangeInitiative;
import com.khartec.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static java.util.Optional.ofNullable;

@Repository
public class ChangeInitiativeDao {

    private static final RecordMapper<Record, ChangeInitiative> TO_DOMAIN_MAPPER = r -> {
        ChangeInitiativeRecord record = r.into(CHANGE_INITIATIVE);
        return ImmutableChangeInitiative.builder()
                .id(record.getId())
                .name(record.getName())
                .description(ofNullable(record.getDescription()).orElse(""))
                .externalId(ofNullable(record.getExternalId()))
                .kind(ChangeInitiativeKind.valueOf(record.getKind()))
                .lifecyclePhase(LifecyclePhase.valueOf(record.getLifecyclePhase()))
                .provenance(record.getProvenance())
                .lastUpdate(record.getLastUpdate())
                .startDate(record.getLastUpdate())
                .endDate(record.getLastUpdate())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ChangeInitiativeDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public ChangeInitiative getById(Long id) {
        return dsl.select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public Collection<ChangeInitiative> findForEntityReference(EntityReference ref) {

        return dsl.select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .innerJoin(ENTITY_RELATIONSHIP)
                .on(ENTITY_RELATIONSHIP.ID_B.eq(CHANGE_INITIATIVE.ID))
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


}
