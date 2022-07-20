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

package org.finos.waltz.data.measurable_rating;

import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.model.*;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.RemoveMeasurableRatingCommand;
import org.finos.waltz.model.measurable_rating.SaveMeasurableRatingCommand;
import org.finos.waltz.model.tally.ImmutableMeasurableRatingTally;
import org.finos.waltz.model.tally.MeasurableRatingTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StringUtilities.firstChar;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.USER_ROLE;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class MeasurableRatingDao {

    private static final Condition APP_JOIN_CONDITION = APPLICATION.ID.eq(MEASURABLE_RATING.ENTITY_ID)
            .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            MEASURABLE_RATING.ENTITY_ID,
            MEASURABLE_RATING.ENTITY_KIND,
            newArrayList(EntityKind.values()))
        .as("entity_name");

    private static final Field<String> ENTITY_LIFECYCLE_FIELD = InlineSelectFieldFactory.mkEntityLifecycleField(
            MEASURABLE_RATING.ENTITY_ID,
            MEASURABLE_RATING.ENTITY_KIND)
            .as("entity_lifecycle_status");


    private static final RecordMapper<? super Record, MeasurableRating> TO_DOMAIN_MAPPER = record -> {
        MeasurableRatingRecord r = record.into(MEASURABLE_RATING);

        EntityReference ref = ImmutableEntityReference.builder()
                .kind(EntityKind.valueOf(r.getEntityKind()))
                .id(r.getEntityId())
                .name(Optional.ofNullable(record.get(ENTITY_NAME_FIELD)))
                .entityLifecycleStatus(readEnum(record.get(ENTITY_LIFECYCLE_FIELD), EntityLifecycleStatus.class, (s) -> EntityLifecycleStatus.REMOVED))
                .build();

        return ImmutableMeasurableRating.builder()
                .entityReference(ref)
                .description(r.getDescription())
                .provenance(r.getProvenance())
                .rating(firstChar(r.getRating(), 'Z'))
                .measurableId(r.getMeasurableId())
                .lastUpdatedAt(toLocalDateTime(r.getLastUpdatedAt()))
                .lastUpdatedBy(r.getLastUpdatedBy())
                .isReadOnly(r.getIsReadonly())
                .build();
    };


    private static final RecordMapper<Record3<Long,String,Integer>, MeasurableRatingTally> TO_TALLY_MAPPER = record -> {
        Long measurableId = record.value1();
        Integer count = record.value3();

        return ImmutableMeasurableRatingTally.builder()
                .id(measurableId)
                .rating(firstChar(record.value2(), 'Z'))
                .count(count)
                .build();
    };


    private final Function<SaveMeasurableRatingCommand, MeasurableRatingRecord> TO_RECORD_MAPPER = command -> {
        MeasurableRatingRecord record = new MeasurableRatingRecord();
        record.setEntityId(command.entityReference().id());
        record.setEntityKind(command.entityReference().kind().name());
        record.setMeasurableId(command.measurableId());
        record.setRating(Character.toString(command.rating()));
        record.setDescription(command.description());
        record.setLastUpdatedAt(Timestamp.valueOf(command.lastUpdate().at()));
        record.setLastUpdatedBy(command.lastUpdate().by());
        record.setProvenance(command.provenance());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public MeasurableRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    // --- save

    public Operation save(SaveMeasurableRatingCommand command, boolean ignoreReadOnly) {
        MeasurableRatingRecord record = TO_RECORD_MAPPER.apply(command);

        boolean exists = checkRatingExists(command);

        if (exists) {
            int updateCount = dsl
                    .update(MEASURABLE_RATING)
                    .set(MEASURABLE_RATING.RATING, String.valueOf(command.rating()))
                    .set(MEASURABLE_RATING.DESCRIPTION, command.description())
                    .set(MEASURABLE_RATING.LAST_UPDATED_BY, command.lastUpdate().by())
                    .set(MEASURABLE_RATING.LAST_UPDATED_AT, command.lastUpdate().atTimestamp())
                    .set(MEASURABLE_RATING.PROVENANCE, command.provenance())
                    .where(MEASURABLE_RATING.ENTITY_ID.eq(command.entityReference().id()))
                    .and(MEASURABLE_RATING.ENTITY_KIND.eq(command.entityReference().kind().name()))
                    .and(MEASURABLE_RATING.MEASURABLE_ID.eq(command.measurableId()))
                    .and(ignoreReadOnly
                            ? DSL.trueCondition()
                            : MEASURABLE_RATING.IS_READONLY.isFalse())
                    .execute();

            if (updateCount == 0) {
                throw new NotFoundException(
                        "MR_SAVE_UPDATE_FAILED",
                        format("Could find writable associated record to update for rating: %s", command));
            };
            return Operation.UPDATE;
        } else {
            if (dsl.executeInsert(record) != 1) {
                throw new NotFoundException(
                        "MR_SAVE_INSERT_FAILED",
                        format("Creation of record failed: %s", command));
            }
            ;
            return Operation.ADD;
        }
    }


    public boolean checkRatingExists(SaveMeasurableRatingCommand command) {
        return dsl
                .fetchExists(DSL
                        .select(MEASURABLE_RATING.fields())
                        .from(MEASURABLE_RATING)
                        .where(MEASURABLE_RATING.MEASURABLE_ID.eq(command.measurableId()))
                        .and(MEASURABLE_RATING.ENTITY_ID.eq(command.entityReference().id()))
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(command.entityReference().kind().name())));
    }


    public boolean remove(RemoveMeasurableRatingCommand command) {
        EntityReference ref = command.entityReference();
        return dsl
                .deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .and(MEASURABLE_RATING.MEASURABLE_ID.eq(command.measurableId()))
                .execute() == 1;
    }


    // --- find

    public List<MeasurableRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return mkBaseQuery()
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<MeasurableRating> findByMeasurableIdSelector(Select<Record1<Long>> selector,
                                                             IdSelectionOptions options) {
        checkNotNull(selector, "selector cannot be null");

        SelectConditionStep<Record> qry = mkBaseQuery()
                .innerJoin(APPLICATION)
                .on(APP_JOIN_CONDITION)
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(selector))
                .and(SelectorUtilities.mkApplicationConditions(options));

        return qry
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<MeasurableRating> findByApplicationIdSelector(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        Condition condition = MEASURABLE_RATING.ENTITY_ID.in(selector)
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(DSL.val(EntityKind.APPLICATION.name())));
        return mkBaseQuery()
                .where(dsl.renderInlined(condition))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<MeasurableRating> findByCategory(long id) {
        return mkBaseQuery()
                .innerJoin(MEASURABLE).on(MEASURABLE_RATING.MEASURABLE_ID.eq(MEASURABLE.ID))
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(id))
                .fetch(TO_DOMAIN_MAPPER);
    }


    // --- stats

    public List<Tally<Long>> tallyByMeasurableCategoryId(long categoryId) {
        SelectHavingStep<Record2<Long, Integer>> query = dsl
                .select(MEASURABLE_RATING.MEASURABLE_ID, DSL.count())
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE)
                .on(MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(MEASURABLE_RATING.ENTITY_ID)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name()))
                .groupBy(MEASURABLE_RATING.MEASURABLE_ID);

        return query.fetch(JooqUtilities.TO_LONG_TALLY);
    }


    public List<MeasurableRatingTally> statsByAppSelector(Select<Record1<Long>> selector) {
        return dsl.select(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.RATING, DSL.count())
                .from(MEASURABLE_RATING)
                .where(dsl.renderInlined(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(MEASURABLE_RATING.ENTITY_ID.in(selector))))
                .groupBy(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.RATING)
                .fetch(TO_TALLY_MAPPER);
    }


    public List<MeasurableRatingTally> statsForRelatedMeasurable(Select<Record1<Long>> selector) {
        org.finos.waltz.schema.tables.MeasurableRating related = MEASURABLE_RATING.as("related");
        org.finos.waltz.schema.tables.MeasurableRating orig = MEASURABLE_RATING.as("orig");

        SelectConditionStep<Record1<Long>> relatedAppIds = DSL
                .selectDistinct(orig.ENTITY_ID)
                .from(orig)
                .where(orig.MEASURABLE_ID.in(selector))
                .and(orig.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return dsl
                .select(related.MEASURABLE_ID,
                        related.RATING,
                        DSL.count())
                .from(related)
                .where(related.ENTITY_ID.in(relatedAppIds))
                .groupBy(related.MEASURABLE_ID,
                        related.RATING)
                .fetch(TO_TALLY_MAPPER);
    }


    public int deleteByMeasurableIdSelector(Select<Record1<Long>> selector) {
        return dsl
                .deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(selector))
                .execute();
    }


    public int removeForCategory(EntityReference ref, long categoryId) {
        SelectConditionStep<Record1<Long>> relevantMeasurableIds = DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId));

        return dsl
                .deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(MEASURABLE_RATING.MEASURABLE_ID.in(relevantMeasurableIds))
                .and(MEASURABLE_RATING.IS_READONLY.isFalse())
                .execute();
    }


    // --- utils

    private SelectJoinStep<Record> mkBaseQuery() {
        return dsl
                .select(MEASURABLE_RATING.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_FIELD)
                .from(MEASURABLE_RATING);
    }

    public Set<Operation> calculateAmendedRatingOperations(Set<Operation> operationsForEntityAssessment,
                                                           EntityReference entityReference,
                                                           long measurableId,
                                                           String username) {

        Field<Boolean> readOnlyRatingField = DSL.coalesce(MEASURABLE_RATING.IS_READONLY, DSL.val(false)).as("rating_read_only");

        SelectConditionStep<Record3<String, Boolean, Boolean>> qry = dsl
                .select(USER_ROLE.ROLE,
                        MEASURABLE_CATEGORY.EDITABLE,
                        readOnlyRatingField)
                .from(MEASURABLE)
                .leftJoin(MEASURABLE_CATEGORY).on(MEASURABLE_CATEGORY.ID.eq(Tables.MEASURABLE.MEASURABLE_CATEGORY_ID))
                .leftJoin(MEASURABLE_RATING).on(Tables.MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(entityReference.kind().name())
                                .and(MEASURABLE_RATING.ENTITY_ID.eq(entityReference.id()))))
                .leftJoin(USER_ROLE).on(USER_ROLE.ROLE.eq(MEASURABLE_CATEGORY.RATING_EDITOR_ROLE)
                        .and(USER_ROLE.USER_NAME.eq(username)))
                .where(MEASURABLE.ID.eq(measurableId));

        System.out.println(qry);

        Tuple3<Boolean, Boolean, Boolean> hasRoleAndDefinitionEditableAndIsReadOnly = qry.fetchOne(r -> tuple(
                notEmpty(r.get(USER_ROLE.ROLE)),
                r.get(MEASURABLE_CATEGORY.EDITABLE),
                r.get(readOnlyRatingField)));

        if (!hasRoleAndDefinitionEditableAndIsReadOnly.v2) {
            return emptySet();
        } else if (hasRoleAndDefinitionEditableAndIsReadOnly.v3) {
            return emptySet();
        } else if (hasRoleAndDefinitionEditableAndIsReadOnly.v1) {
            return union(operationsForEntityAssessment, asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));
        } else {
            return operationsForEntityAssessment;
        }
    }

}
