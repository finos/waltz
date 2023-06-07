package org.finos.waltz.data.involvement_group;

import org.finos.waltz.model.involvement_group.ImmutableInvolvementGroup;
import org.finos.waltz.model.involvement_group.InvolvementGroup;
import org.finos.waltz.model.involvement_group.InvolvementGroupCreateCommand;
import org.finos.waltz.schema.tables.records.InvolvementGroupEntryRecord;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.finos.waltz.common.CollectionUtilities.notEmpty;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.schema.Tables.INVOLVEMENT_GROUP;
import static org.finos.waltz.schema.Tables.INVOLVEMENT_GROUP_ENTRY;

@Repository
public class InvolvementGroupDao {

    public final DSLContext dsl;

    public static final RecordMapper<Record, InvolvementGroup> TO_DOMAIN_MAPPER = r -> {
        InvolvementGroupRecord record = r.into(InvolvementGroupRecord.class);

        return ImmutableInvolvementGroup.builder()
                .id(record.getId())
                .name(record.getName())
                .provenance(record.getProvenance())
                .externalId(record.getExternalId())
                .build();
    };

    public static final Function<InvolvementGroup, InvolvementGroupRecord> TO_RECORD_MAPPER = invGroup -> {

        InvolvementGroupRecord record = new InvolvementGroupRecord();
        record.setName(invGroup.name());
        record.setProvenance(invGroup.provenance());
        invGroup.id().ifPresent(record::setId);
        record.setExternalId(invGroup.externalId());

        return record;
    };

    public InvolvementGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Long createInvolvementGroup(InvolvementGroupCreateCommand cmd) {

        InvolvementGroupRecord invGroupRecord = TO_RECORD_MAPPER.apply(cmd.involvementGroup());

        Long invGroupId = dsl
                .insertInto(INVOLVEMENT_GROUP)
                .set(invGroupRecord)
                .returning(INVOLVEMENT_GROUP.ID)
                .fetchOne()
                .getId();

        if (invGroupId != null && notEmpty(cmd.involvementKindIds())) {

            int inserted = summarizeResults(cmd.involvementKindIds()
                    .stream()
                    .map(kindId -> {
                        InvolvementGroupEntryRecord entry = dsl.newRecord(INVOLVEMENT_GROUP_ENTRY);
                        entry.setInvolvementGroupId(invGroupId);
                        entry.setInvolvementKindId(kindId);
                        return entry;
                    })
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), dsl::batchInsert))
                    .execute());
        }

        return invGroupId;
    }

    public void updateInvolvements(Long groupId, Set<Long> involvementKindIds) {

        dsl.transaction(ctx -> {
            DSLContext tx = ctx.dsl();

            int removed = tx
                    .deleteFrom(INVOLVEMENT_GROUP_ENTRY)
                    .where(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID.eq(groupId))
                    .execute();

            int[] inserted = involvementKindIds
                    .stream()
                    .map(kindId -> {
                        InvolvementGroupEntryRecord r = tx.newRecord(INVOLVEMENT_GROUP_ENTRY);
                        r.setInvolvementGroupId(groupId);
                        r.setInvolvementKindId(kindId);
                        return r;
                    })
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), tx::batchInsert))
                    .execute();
        });
    }
}
