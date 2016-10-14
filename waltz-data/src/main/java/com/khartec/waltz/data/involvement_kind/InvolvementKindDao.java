package com.khartec.waltz.data.involvement_kind;

import com.khartec.waltz.model.invovement_kind.ImmutableInvolvementKind;
import com.khartec.waltz.model.invovement_kind.InvolvementKind;
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
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
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


    public InvolvementKind create(InvolvementKind involvementKind) {
        checkNotNull(involvementKind, "involvementKind cannot be null");

        if(involvementKind.id().isPresent())
            throw new IllegalArgumentException("Involvement Kind record already has an ID, cannot create a duplicate");

        InvolvementKindRecord record = TO_RECORD_MAPPER.apply(involvementKind);
        dsl.attach(record);
        record.store();

        return TO_DOMAIN_MAPPER.map(record);
    }


    public InvolvementKind update(InvolvementKind involvementKind) {
        checkNotNull(involvementKind, "involvementKind cannot be null");

        long id = involvementKind.id()
                .orElseThrow(() ->  new IllegalArgumentException("Involvement Kind missing id, cannot update"));

        InvolvementKindRecord record = dsl.select(INVOLVEMENT_KIND.fields())
                .from(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.ID.eq(id))
                .fetchOneInto(InvolvementKindRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Involvement Kind record with id: " + id);
        }

        record.setName(involvementKind.name());
        record.setDescription(involvementKind.description());
        record.setLastUpdatedAt(Timestamp.valueOf(involvementKind.lastUpdatedAt()));
        record.setLastUpdatedBy(involvementKind.lastUpdatedBy());
        record.update();
        return involvementKind;
    }


    public boolean deleteIfNotUsed(long id) {
        return dsl.deleteFrom(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.ID.eq(id))
                .and(DSL.notExists(DSL.selectFrom(INVOLVEMENT).where(INVOLVEMENT.KIND_ID.eq(id))))
                .execute() > 0;
    }

}
