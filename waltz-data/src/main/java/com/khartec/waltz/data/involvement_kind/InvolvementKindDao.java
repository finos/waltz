package com.khartec.waltz.data.involvement_kind;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.involvement_kind.ImmutableInvolvementKind;
import com.khartec.waltz.model.involvement_kind.InvolvementKind;
import com.khartec.waltz.model.involvement_kind.InvolvementKindChangeCommand;
import com.khartec.waltz.model.involvement_kind.InvolvementKindCreateCommand;
import com.khartec.waltz.schema.tables.records.InvolvementKindRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkOptionalIsPresent;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;

@Repository
public class InvolvementKindDao {

    public static final com.khartec.waltz.schema.tables.InvolvementKind involvementKind = INVOLVEMENT_KIND.as("inv_kind");

    public static final RecordMapper<Record, InvolvementKind> TO_DOMAIN_MAPPER = r -> {
        InvolvementKindRecord record = r.into(InvolvementKindRecord.class);

        return ImmutableInvolvementKind.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };


    public static final Function<InvolvementKind, InvolvementKindRecord> TO_RECORD_MAPPER = ik -> {

        InvolvementKindRecord record = new InvolvementKindRecord();
        record.setName(ik.name());
        record.setDescription(ik.description());
        record.setLastUpdatedAt(Timestamp.valueOf(ik.lastUpdatedAt()));
        record.setLastUpdatedBy(ik.lastUpdatedBy());

        ik.id().ifPresent(record::setId);

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public InvolvementKindDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<InvolvementKind> findAll() {
        return dsl.select(involvementKind.fields())
                .from(involvementKind)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public InvolvementKind getById(long id) {
        InvolvementKindRecord record = dsl.select(INVOLVEMENT_KIND.fields())
                .from(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.ID.eq(id))
                .fetchOneInto(InvolvementKindRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Involvement Kind record with id: " + id);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }


    public Long create(InvolvementKindCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        InvolvementKindRecord record = dsl.newRecord(INVOLVEMENT_KIND);
        record.setName(command.name());
        record.setDescription(command.description());
        record.setLastUpdatedBy(username);
        record.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        record.store();

        return record.getId();
    }


    public boolean update(InvolvementKindChangeCommand command) {
        checkNotNull(command, "command cannot be null");
        checkOptionalIsPresent(command.lastUpdate(), "lastUpdate must be present");

        InvolvementKindRecord record = dsl.newRecord(INVOLVEMENT_KIND);
        record.setId(command.id());
        record.changed(INVOLVEMENT_KIND.ID, false);

        command.name().ifPresent(change -> record.setName(change.newVal()));
        command.description().ifPresent(change -> record.setDescription(change.newVal()));

        LastUpdate lastUpdate = command.lastUpdate().get();
        record.setLastUpdatedAt(Timestamp.valueOf(lastUpdate.at()));
        record.setLastUpdatedBy(lastUpdate.by());

        int count = record.update();
        return count == 1;
    }


    public boolean deleteIfNotUsed(long id) {
        return dsl.deleteFrom(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.ID.eq(id))
                .and(DSL.notExists(DSL.selectFrom(INVOLVEMENT).where(INVOLVEMENT.KIND_ID.eq(id))))
                .execute() > 0;
    }


}
