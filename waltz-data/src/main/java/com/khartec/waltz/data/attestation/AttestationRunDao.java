package com.khartec.waltz.data.attestation;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.attestation.AttestationRun;
import com.khartec.waltz.model.attestation.AttestationRunCreateCommand;
import com.khartec.waltz.model.attestation.ImmutableAttestationRun;
import com.khartec.waltz.schema.tables.records.AttestationRunRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.StringUtilities.join;
import static com.khartec.waltz.common.StringUtilities.splitThenMap;
import static com.khartec.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;

@Repository
public class AttestationRunDao {

    private static final String ID_SEPARATOR = ";";

    private static final RecordMapper<Record, AttestationRun> TO_DOMAIN_MAPPER = r -> {
        AttestationRunRecord record = r.into(ATTESTATION_RUN);

        return ImmutableAttestationRun.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .selectionOptions(IdSelectionOptions.mkOpts(
                        EntityReference.mkRef(
                                EntityKind.valueOf(record.getSelectorEntityKind()),
                                record.getSelectorEntityId()),
                        HierarchyQueryScope.valueOf(record.getSelectorHierarchyScope())))
                .involvementKindIds(splitThenMap(
                        record.getInvolvementKindIds(),
                        ID_SEPARATOR,
                        Long::valueOf))
                .issuedOn(record.getIssuedOn().toLocalDate())
                .dueDate(record.getDueDate().toLocalDate())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public AttestationRunDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public AttestationRun getById(long attestationRunId) {
        return dsl.selectFrom(ATTESTATION_RUN)
                .where(ATTESTATION_RUN.ID.eq(attestationRunId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Long create(String userId, AttestationRunCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        AttestationRunRecord record = dsl.newRecord(ATTESTATION_RUN);
        record.setTargetEntityKind(command.targetEntityKind().name());
        record.setName(command.name());
        record.setDescription(command.description());
        record.setSelectorEntityKind(command.selectionOptions().entityReference().kind().name());
        record.setSelectorEntityId(command.selectionOptions().entityReference().id());
        record.setSelectorHierarchyScope(command.selectionOptions().scope().name());
        record.setInvolvementKindIds(join(command.involvementKindIds(), ID_SEPARATOR));
        record.setIssuedBy(userId);
        record.setIssuedOn(toSqlDate(command.issuedOn()));
        record.setDueDate(toSqlDate(command.dueDate()));

        record.insert();

        return record.getId();
    }
}
