package com.khartec.waltz.data.entity_event;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_event.EntityEvent;
import com.khartec.waltz.model.entity_event.ImmutableEntityEvent;
import com.khartec.waltz.schema.tables.records.EntityEventRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityEvent.ENTITY_EVENT;

@Repository
public class EntityEventDao {

    private static final RecordMapper<EntityEventRecord, EntityEvent> TO_DOMAIN_MAPPER = r -> {
        return ImmutableEntityEvent
                .builder()
                .entityReference(EntityReference.mkRef(
                        EntityKind.valueOf(r.getEntityKind()),
                        r.getEntityId()))
                .eventType(r.getEventType())
                .eventDate(r.getEventDate().toLocalDateTime())
                .description(r.getDescription())
                .provenance(r.getProvenance())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(r.getLastUpdatedBy())
                .build();
    };



    private final DSLContext dsl;


    @Autowired
    public EntityEventDao(DSLContext dsl) {
        this.dsl = dsl;
    }



    public List<EntityEvent> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl.selectFrom(ENTITY_EVENT)
                .where(ENTITY_EVENT.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_EVENT.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
