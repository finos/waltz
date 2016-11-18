package com.khartec.waltz.data.actor;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.actor.ActorChangeCommand;
import com.khartec.waltz.model.actor.ActorCreateCommand;
import com.khartec.waltz.model.actor.ImmutableActor;
import com.khartec.waltz.schema.tables.records.ActorRecord;
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
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;

@Repository
public class ActorDao {

    public static final com.khartec.waltz.schema.tables.Actor actor = ACTOR.as("actor");

    public static final RecordMapper<Record, Actor> TO_DOMAIN_MAPPER = r -> {
        ActorRecord record = r.into(ActorRecord.class);

        return ImmutableActor.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isExternal(record.getIsExternal())
                .build();
    };


    public static final Function<Actor, ActorRecord> TO_RECORD_MAPPER = ik -> {

        ActorRecord record = new ActorRecord();
        record.setName(ik.name());
        record.setDescription(ik.description());
        record.setLastUpdatedAt(Timestamp.valueOf(ik.lastUpdatedAt()));
        record.setLastUpdatedBy(ik.lastUpdatedBy());
        record.setIsExternal(ik.isExternal());

        ik.id().ifPresent(record::setId);

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public ActorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<Actor> findAll() {
        return dsl.select(actor.fields())
                .from(actor)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Actor getById(long id) {
        ActorRecord record = dsl.select(ACTOR.fields())
                .from(ACTOR)
                .where(ACTOR.ID.eq(id))
                .fetchOneInto(ActorRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Actor record with id: " + id);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }


    public Long create(ActorCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        ActorRecord record = dsl.newRecord(ACTOR);
        record.setName(command.name());
        record.setDescription(command.description());
        record.setIsExternal(command.isExternal());
        record.setLastUpdatedBy(username);
        record.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        record.store();

        return record.getId();
    }


    public boolean update(ActorChangeCommand command) {
        checkNotNull(command, "command cannot be null");
        checkOptionalIsPresent(command.lastUpdate(), "lastUpdate must be present");

        ActorRecord record = dsl.newRecord(ACTOR);
        record.setId(command.id());
        record.changed(ACTOR.ID, false);

        command.name().ifPresent(change -> record.setName(change.newVal()));
        command.description().ifPresent(change -> record.setDescription(change.newVal()));
        command.isExternal().ifPresent(change -> record.setIsExternal(change.newVal()));

        LastUpdate lastUpdate = command.lastUpdate().get();
        record.setLastUpdatedAt(Timestamp.valueOf(lastUpdate.at()));
        record.setLastUpdatedBy(lastUpdate.by());

        int count = record.update();
        return count == 1;
    }


    public boolean deleteIfNotUsed(long id) {
        return dsl.deleteFrom(ACTOR)
                .where(ACTOR.ID.eq(id))
                .and(DSL.notExists(DSL.selectFrom(INVOLVEMENT).where(INVOLVEMENT.KIND_ID.eq(id))))
                .execute() > 0;
    }


}
