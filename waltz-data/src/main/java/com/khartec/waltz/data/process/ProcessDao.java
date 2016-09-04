package com.khartec.waltz.data.process;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.FindEntityReferencesByIdSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.model.process.ImmutableProcess;
import com.khartec.waltz.model.process.Process;
import com.khartec.waltz.schema.tables.EntityRelationship;
import com.khartec.waltz.schema.tables.records.ProcessRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.Process.PROCESS;

@Repository
public class ProcessDao implements FindEntityReferencesByIdSelector {

    private static final com.khartec.waltz.schema.tables.Process p = PROCESS.as("p");
    private static final EntityRelationship rel = EntityRelationship.ENTITY_RELATIONSHIP.as("rel");

    public static final RecordMapper<? super Record, Process> TO_DOMAIN = r -> {
        ProcessRecord record = r.into(PROCESS);
        return ImmutableProcess.builder()
                .id(record.getId())
                .parentId(Optional.ofNullable(record.getParentId()))
                .name(record.getName())
                .description(mkSafe(record.getDescription()))
                .level(record.getLevel())
                .level1(record.getLevel_1())
                .level2(Optional.ofNullable(record.getLevel_2()))
                .level3(Optional.ofNullable(record.getLevel_3()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ProcessDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Process getById(long id) {
        return dsl.select(p.fields())
                .from(p)
                .where(p.ID.eq(id))
                .fetchOne(TO_DOMAIN);
    }


    public List<Process> findAll() {
        return dsl.select(p.fields())
                .from(p)
                .orderBy(p.NAME.asc())
                .fetch(TO_DOMAIN);
    }


    public Collection<Process> findForCapability(long id) {
        return dsl.select(p.fields())
                .from(p)
                .innerJoin(rel)
                .on(rel.ID_B.eq(p.ID))
                .where(rel.KIND_B.eq(EntityKind.PROCESS.name()))
                .and(rel.KIND_A.eq(EntityKind.CAPABILITY.name()))
                .and(rel.ID_A.eq(id))
                .and(rel.RELATIONSHIP.eq(RelationshipKind.SUPPORTS.name()))
                .fetch(TO_DOMAIN);
    }


    public Collection<Process> findForApplication(long id) {
        return dsl.select(p.fields())
                .from(p)
                .innerJoin(rel)
                .on(rel.ID_B.eq(p.ID))
                .where(rel.KIND_B.eq(EntityKind.PROCESS.name()))
                .and(rel.KIND_A.eq(EntityKind.APPLICATION.name()))
                .and(rel.ID_A.eq(id))
                .and(rel.RELATIONSHIP.eq(RelationshipKind.PARTICIPATES_IN.name()))
                .fetch(TO_DOMAIN);
    }


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl.select(p.ID, p.NAME, DSL.val(EntityKind.PROCESS.name()))
                .from(p)
                .where(p.ID.in(selector))
                .fetch(TO_ENTITY_REFERENCE);
    }

}
