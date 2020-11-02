/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.data.measurable_rating_planned_decommission;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.exception.ModifyingReadOnlyRecordException;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.command.DateFieldChange;
import com.khartec.waltz.model.measurable_rating_planned_decommission.ImmutableMeasurableRatingPlannedDecommission;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.schema.tables.records.MeasurableRatingPlannedDecommissionRecord;
import org.jooq.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static com.khartec.waltz.schema.tables.MeasurableRatingPlannedDecommission.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static com.khartec.waltz.schema.tables.MeasurableRatingReplacement.MEASURABLE_RATING_REPLACEMENT;

@Repository
public class MeasurableRatingPlannedDecommissionDao {

    private static final Field<String> NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_ID,
            MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_KIND,
            asSet(EntityKind.APPLICATION));

    public static final RecordMapper<? super Record, MeasurableRatingPlannedDecommission> TO_DOMAIN_MAPPER = record -> {

        MeasurableRatingPlannedDecommissionRecord r = record.into(MEASURABLE_RATING_PLANNED_DECOMMISSION);

        return ImmutableMeasurableRatingPlannedDecommission.builder()
                .id(r.getId())
                .entityReference(mkRef(
                        EntityKind.valueOf(r.getEntityKind()),
                        r.getEntityId(),
                        record.get(NAME_FIELD)))
                .measurableId(r.getMeasurableId())
                .plannedDecommissionDate(r.getPlannedDecommissionDate())
                .createdAt(toLocalDateTime(r.getCreatedAt()))
                .createdBy(r.getCreatedBy())
                .lastUpdatedAt(toLocalDateTime(r.getUpdatedAt()))
                .lastUpdatedBy(r.getUpdatedBy())
                .build();
    };


    private final DSLContext dsl;



    @Autowired
    MeasurableRatingPlannedDecommissionDao(DSLContext dsl){
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public static void checkIfReadOnly(DSLContext myDsl, long id) {
        Boolean readOnly = myDsl
                .select(MEASURABLE_RATING.IS_READONLY)
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .on(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_ID.eq(MEASURABLE_RATING.ENTITY_ID))
                .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_KIND.eq(MEASURABLE_RATING.ENTITY_KIND))
                .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(id))
                .fetchOne(MEASURABLE_RATING.IS_READONLY);
        if (readOnly) {
            throw new ModifyingReadOnlyRecordException("MRPD_READ_ONLY", "Cannot update the measurable rating planned decommission as it is read only");
        }
    }


    public MeasurableRatingPlannedDecommission getById(Long id){
        return dsl
                .select(MEASURABLE_RATING_PLANNED_DECOMMISSION.fields())
                .select(NAME_FIELD)
                .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public MeasurableRatingPlannedDecommission getByEntityAndMeasurable(EntityReference entityReference,
                                                                        long measurableId) {
        return dsl
                .select(MEASURABLE_RATING_PLANNED_DECOMMISSION.fields())
                .select(NAME_FIELD)
                .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(mkRefCondition(entityReference)
                        .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_ID.eq(measurableId)))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<MeasurableRatingPlannedDecommission> findByEntityRef(EntityReference ref){
        return dsl
                .select(MEASURABLE_RATING_PLANNED_DECOMMISSION.fields())
                .select(NAME_FIELD)
                .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(mkRefCondition(ref))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Collection<MeasurableRatingPlannedDecommission> findByReplacingEntityRef(EntityReference ref) {
        return dsl
                .select(MEASURABLE_RATING_PLANNED_DECOMMISSION.fields())
                .select(NAME_FIELD)
                .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .innerJoin(MEASURABLE_RATING_REPLACEMENT)
                    .on(MEASURABLE_RATING_REPLACEMENT.DECOMMISSION_ID.eq(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID))
                .where(MEASURABLE_RATING_REPLACEMENT.ENTITY_ID.eq(ref.id()))
                .and(MEASURABLE_RATING_REPLACEMENT.ENTITY_KIND.eq(ref.kind().name()))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Tuple2<Operation, Boolean> save(EntityReference entityReference,
                                           long measurableId,
                                           DateFieldChange dateChange,
                                           String userName) {
        checkIfReadOnly(entityReference, measurableId);

        MeasurableRatingPlannedDecommissionRecord existingRecord = dsl
                .selectFrom(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(mkRefCondition(entityReference)
                        .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_ID.eq(measurableId)))
                .fetchOne();

        if (existingRecord != null) {
            updateDecommDateOnRecord(existingRecord, dateChange, userName);
            boolean updatedRecord = existingRecord.update() == 1;
            return Tuple.tuple(Operation.UPDATE, updatedRecord);
        } else {
            MeasurableRatingPlannedDecommissionRecord record = dsl.newRecord(MEASURABLE_RATING_PLANNED_DECOMMISSION);
            updateDecommDateOnRecord(record, dateChange, userName);
            record.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
            record.setCreatedBy(userName);
            record.setEntityId(entityReference.id());
            record.setEntityKind(entityReference.kind().name());
            record.setMeasurableId(measurableId);
            boolean recordsInserted = record.insert() == 1;

            return Tuple.tuple(Operation.ADD, recordsInserted);
        }
    }


    public boolean remove(Long id){
        checkIfReadOnly(dsl, id);
        return dsl
                .deleteFrom(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(id))
                .execute() == 1;
    }


    // -- HELPERS ----

    private Condition mkRefCondition(EntityReference ref) {
        return MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_ID.eq(ref.id())
                .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_KIND.eq(ref.kind().name()));
    }


    private void checkIfReadOnly(EntityReference entityReference, long measurableId) {
        Boolean readOnly = dsl
                .select(MEASURABLE_RATING.IS_READONLY)
                .from(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(entityReference.id()))
                .and(MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId))
                .fetchOne(MEASURABLE_RATING.IS_READONLY);

        if (readOnly) {
            throw new ModifyingReadOnlyRecordException("MRPD_READ_ONLY", "Cannot update measurable rating planned decomm date as it is read only");
        }
    }



    private void updateDecommDateOnRecord(MeasurableRatingPlannedDecommissionRecord record,
                                          DateFieldChange dateChange,
                                          String userName) {
        record.setUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setUpdatedBy(userName);
        record.setPlannedDecommissionDate(toSqlDate(dateChange.newVal()));
    }

}
