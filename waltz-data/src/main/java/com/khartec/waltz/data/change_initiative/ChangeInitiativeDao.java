package com.khartec.waltz.data.change_initiative;

import com.khartec.waltz.data.FindEntityReferencesByIdSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.change_initiative.ChangeInitiativeKind;
import com.khartec.waltz.model.change_initiative.ImmutableChangeInitiative;
import com.khartec.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static java.util.Optional.ofNullable;

@Repository
public class ChangeInitiativeDao implements FindEntityReferencesByIdSelector {

    public static final RecordMapper<Record, ChangeInitiative> TO_DOMAIN_MAPPER = r -> {
        ChangeInitiativeRecord record = r.into(CHANGE_INITIATIVE);
        return ImmutableChangeInitiative.builder()
                .id(record.getId())
                .name(record.getName())
                .description(ofNullable(record.getDescription()).orElse(""))
                .externalId(ofNullable(record.getExternalId()))
                .kind(ChangeInitiativeKind.valueOf(record.getKind()))
                .lifecyclePhase(LifecyclePhase.valueOf(record.getLifecyclePhase()))
                .provenance(record.getProvenance())
                .lastUpdate(ofNullable(record.getLastUpdate()))
                .startDate(record.getStartDate())
                .endDate(record.getEndDate())
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


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl.select(CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.NAME, DSL.val(EntityKind.CHANGE_INITIATIVE.name()))
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(selector))
                .fetch(TO_ENTITY_REFERENCE);
    }


}
