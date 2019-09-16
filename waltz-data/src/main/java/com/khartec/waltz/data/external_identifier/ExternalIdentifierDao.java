package com.khartec.waltz.data.external_identifier;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.external_identifier.ImmutableExternalIdentifier;
import com.khartec.waltz.schema.tables.records.ExternalIdentifierRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.ExternalIdentifier.EXTERNAL_IDENTIFIER;

@Repository
public class ExternalIdentifierDao {


    private final DSLContext dsl;

    @Autowired
    public ExternalIdentifierDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public int create(ExternalIdentifier externalIdentifier) {
        ExternalIdentifierRecord record = new ExternalIdentifierRecord();

        record.setEntityId(externalIdentifier.entityReference().id());
        record.setEntityKind(externalIdentifier.entityReference().kind().name());
        record.setExternalId(externalIdentifier.externalId());
        record.setSystem(externalIdentifier.system());

        return record.store();
    }


    public int[] create(Set<ExternalIdentifier> externalIdentifiers) {
        return dsl.batchInsert(convertToExternalIdentifierRecords(externalIdentifiers)).execute();
    }


    public List<ExternalIdentifier> findByEntityReference(EntityReference entityRef) {
        return dsl
                .select(EXTERNAL_IDENTIFIER.fields())
                .from(EXTERNAL_IDENTIFIER)
                .where(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(entityRef.id()))
                .and(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(entityRef.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int[] delete(List<ExternalIdentifier> externalIdentifiers) {
        return dsl.batchDelete(convertToExternalIdentifierRecords(externalIdentifiers)).execute();

    }


    private Set<ExternalIdentifierRecord> convertToExternalIdentifierRecords(Collection<ExternalIdentifier> externalIdentifiers) {
        return externalIdentifiers.stream()
                .map(this::getExternalIdentifierRecord)
                .collect(Collectors.toSet());
    }


    private ExternalIdentifierRecord getExternalIdentifierRecord(ExternalIdentifier externalIdentifier) {

        ExternalIdentifierRecord record = new ExternalIdentifierRecord();
        record.setEntityId(externalIdentifier.entityReference().id());
        record.setEntityKind(externalIdentifier.entityReference().kind().name());
        record.setExternalId(externalIdentifier.externalId());
        record.setSystem(externalIdentifier.system());
        return record;
    }

    private static final RecordMapper<Record, ExternalIdentifier> TO_DOMAIN_MAPPER = r -> {
        ExternalIdentifierRecord record = r.into(EXTERNAL_IDENTIFIER);
        return ImmutableExternalIdentifier.builder()
                .externalId(record.getExternalId())
                .entityReference(JooqUtilities.readRef(record, EXTERNAL_IDENTIFIER.ENTITY_KIND, EXTERNAL_IDENTIFIER.ENTITY_ID))
                .system(record.getSystem())
                .build();
    };
}
