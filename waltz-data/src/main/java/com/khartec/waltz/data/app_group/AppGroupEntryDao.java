package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;


@Repository
public class AppGroupEntryDao {

    private static final RecordMapper<Record, EntityReference> appRefMapper = r ->
            ImmutableEntityReference.builder()
                    .kind(EntityKind.APPLICATION)
                    .id(r.getValue(APPLICATION.ID))
                    .name(r.getValue(APPLICATION.NAME))
                    .build();

    private final DSLContext dsl;


    @Autowired
    public AppGroupEntryDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityReference> getEntriesForGroup(long groupId) {
        return dsl.select(APPLICATION.ID, APPLICATION.NAME)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(DSL.select(APPLICATION_GROUP_ENTRY.APPLICATION_ID)
                        .from(APPLICATION_GROUP_ENTRY)
                        .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))))
                .fetch(appRefMapper);
    }
}
