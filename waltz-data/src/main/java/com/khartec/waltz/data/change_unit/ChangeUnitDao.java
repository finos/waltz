/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.change_unit;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.change_set.ChangeSet;
import com.khartec.waltz.model.change_unit.*;
import com.khartec.waltz.schema.tables.records.ChangeSetRecord;
import com.khartec.waltz.schema.tables.records.ChangeUnitRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.*;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.ChangeUnit.CHANGE_UNIT;


@Repository
public class ChangeUnitDao {

    private final DSLContext dsl;

    public static final RecordMapper<Record, ChangeUnit> TO_DOMAIN_MAPPER = r -> {
        ChangeUnitRecord record = r.into(ChangeUnitRecord.class);

        EntityReference subjectRef = mkRef(
                EntityKind.valueOf(record.getSubjectEntityKind()),
                record.getSubjectEntityId());

        return ImmutableChangeUnit.builder()
                .id(record.getId())
                .changeSetId(record.getChangeSetId())
                .subjectEntity(subjectRef)
                .subjectInitialStatus(EntityLifecycleStatus.valueOf(record.getSubjectInitialStatus()))
                .action(ChangeAction.valueOf(record.getAction()))
                .executionStatus(ExecutionStatus.valueOf(record.getExecutionStatus()))
                .name(record.getName())
                .description(record.getDescription())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .kind(EntityKind.CHANGE_UNIT)
                .build();
    };


    public static final Function<ChangeSet, ChangeSetRecord> TO_RECORD_MAPPER = changeSet -> {

        ChangeSetRecord record = new ChangeSetRecord();
        changeSet.id().ifPresent(record::setId);
        changeSet.parentEntity().ifPresent(ref -> record.setParentEntityKind(ref.kind().name()));
        changeSet.parentEntity().ifPresent(ref -> record.setParentEntityId(ref.id()));
        record.setPlannedDate(changeSet.plannedDate().map(t -> Timestamp.valueOf(t)).orElse(null));
        record.setEntityLifecycleStatus(changeSet.entityLifecycleStatus().name());
        record.setName(changeSet.name());
        record.setDescription(changeSet.description());
        record.setLastUpdatedAt(Timestamp.valueOf(changeSet.lastUpdatedAt()));
        record.setLastUpdatedBy(changeSet.lastUpdatedBy());
        record.setExternalId(changeSet.externalId().orElse(null));
        record.setProvenance(changeSet.provenance());

        return record;
    };


    @Autowired
    public ChangeUnitDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public ChangeUnit getById(long id) {
        return dsl
                .selectFrom(CHANGE_UNIT)
                .where(CHANGE_UNIT.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<ChangeUnit> findBySubjectRef(EntityReference ref) {
        return dsl.selectFrom(CHANGE_UNIT)
                .where(CHANGE_UNIT.SUBJECT_ENTITY_ID.eq(ref.id()))
                .and(CHANGE_UNIT.SUBJECT_ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeUnit> findByChangeSetId(long id) {
        return dsl.selectFrom(CHANGE_UNIT)
                .where(CHANGE_UNIT.CHANGE_SET_ID.eq(id))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeUnit> findBySelector(Select<Record1<Long>> selector) {
        return dsl.selectFrom(CHANGE_UNIT)
                .where(CHANGE_UNIT.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean updateExecutionStatus(UpdateExecutionStatusCommand command) {
        checkNotNull(command, "command cannot be null");
        checkOptionalIsPresent(command.lastUpdate(), "lastUpdate must be present");
        checkTrue(command.executionStatus().oldVal().equals(ExecutionStatus.PENDING), "Current status should be PENDING");

        UserTimestamp lastUpdate = command.lastUpdate().get();

        int count = dsl.update(CHANGE_UNIT)
                .set(CHANGE_UNIT.EXECUTION_STATUS, command.executionStatus().newVal().name())
                .set(CHANGE_UNIT.LAST_UPDATED_AT, Timestamp.valueOf(lastUpdate.at()))
                .set(CHANGE_UNIT.LAST_UPDATED_BY, lastUpdate.by())
                .where(CHANGE_UNIT.EXECUTION_STATUS.eq(command.executionStatus().oldVal().name()))
                    .and(CHANGE_UNIT.ID.eq(command.id()))
                .execute();

        return count == 1;
    }
}
