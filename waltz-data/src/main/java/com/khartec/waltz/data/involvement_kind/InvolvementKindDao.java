package com.khartec.waltz.data.involvement_kind;

import com.khartec.waltz.model.invovement_kind.ImmutableInvolvementKind;
import com.khartec.waltz.model.invovement_kind.InvolvementKind;
import com.khartec.waltz.schema.tables.records.InvolvementKindRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
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
}
