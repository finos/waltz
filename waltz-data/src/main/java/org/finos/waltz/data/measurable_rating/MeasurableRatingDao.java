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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingChangeSummary;
import org.finos.waltz.model.measurable_rating.RemoveMeasurableRatingCommand;
import org.finos.waltz.model.measurable_rating.SaveMeasurableRatingCommand;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.tally.ImmutableMeasurableRatingTally;
import org.finos.waltz.model.tally.MeasurableRatingTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.AllocationRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingPlannedDecommissionRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;

import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

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
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.Tables.ALLOCATION;
import static org.finos.waltz.schema.Tables.CHANGE_LOG;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static org.finos.waltz.schema.Tables.RATING_SCHEME_ITEM;
import static org.finos.waltz.schema.Tables.USER_ROLE;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

import org.jooq.Query;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Record9;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOrderByStep;
import org.jooq.UpdateConditionStep;
import static org.jooq.impl.DSL.select;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class MeasurableRatingDao {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableRatingDao.class);

    private static final Condition APP_JOIN_CONDITION = APPLICATION.ID.eq(MEASURABLE_RATING.ENTITY_ID)
            .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

    private static final ArrayList<EntityKind> SUPPORTED_ENTITY_KINDS = newArrayList(
            EntityKind.APPLICATION,
            EntityKind.END_USER_APPLICATION,
            EntityKind.ACTOR,
            EntityKind.CHANGE_INITIATIVE);

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    MEASURABLE_RATING.ENTITY_ID,
                    MEASURABLE_RATING.ENTITY_KIND,
                    SUPPORTED_ENTITY_KINDS)
            .as("entity_name");

    private static final Field<String> ENTITY_LIFECYCLE_FIELD = InlineSelectFieldFactory.mkEntityLifecycleField(
                    MEASURABLE_RATING.ENTITY_ID,
                    MEASURABLE_RATING.ENTITY_KIND,
                    SUPPORTED_ENTITY_KINDS)
            .as("entity_lifecycle_status");

    private static final Field<String> ENTITY_DESCRIPTION_FIELD = InlineSelectFieldFactory.mkDescriptionField(
                    MEASURABLE_RATING.ENTITY_ID,
                    MEASURABLE_RATING.ENTITY_KIND,
                    SUPPORTED_ENTITY_KINDS)
            .as("entity_description");

    private static final Field<String> ENTITY_EXT_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
                    MEASURABLE_RATING.ENTITY_ID,
                    MEASURABLE_RATING.ENTITY_KIND,
                    SUPPORTED_ENTITY_KINDS)
            .as("entity_ext_id");


    private static final RecordMapper<? super Record, MeasurableRating> TO_DOMAIN_MAPPER = record -> {
        MeasurableRatingRecord r = record.into(MEASURABLE_RATING);

        EntityReference ref = ImmutableEntityReference.builder()
                .kind(EntityKind.valueOf(r.getEntityKind()))
                .id(r.getEntityId())
                .name(Optional.ofNullable(record.get(ENTITY_NAME_FIELD)))
                .entityLifecycleStatus(readEnum(record.get(ENTITY_LIFECYCLE_FIELD), EntityLifecycleStatus.class, (s) -> EntityLifecycleStatus.REMOVED))
                .description(record.get(ENTITY_DESCRIPTION_FIELD))
                .externalId(Optional.ofNullable(record.get(ENTITY_EXT_ID_FIELD)))
                .build();

        Long ratingId = record.get(RATING_SCHEME_ITEM.ID);

        return ImmutableMeasurableRating.builder()
                .id(r.getId())
                .entityReference(ref)
                .description(r.getDescription())
                .provenance(r.getProvenance())
                .rating(firstChar(r.getRating(), 'Z'))
                .measurableId(r.getMeasurableId())
                .lastUpdatedAt(toLocalDateTime(r.getLastUpdatedAt()))
                .lastUpdatedBy(r.getLastUpdatedBy())
                .isReadOnly(r.getIsReadonly())
                .isPrimary(r.getIsPrimary())
                .ratingId(ratingId)
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
        record.setIsPrimary(command.isPrimary());
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
                    .set(MEASURABLE_RATING.IS_PRIMARY, command.isPrimary())
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
            }
            return Operation.UPDATE;
        } else {
            if (dsl.executeInsert(record) != 1) {
                throw new NotFoundException(
                        "MR_SAVE_INSERT_FAILED",
                        format("Creation of record failed: %s", command));
            }

            return Operation.ADD;
        }
    }


    public boolean checkRatingExists(SaveMeasurableRatingCommand command) {
        return dsl
                .fetchExists(select(MEASURABLE_RATING.fields())
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
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    /*
     * Should move to using a measurable rating id selector
     */
    @Deprecated
    public List<MeasurableRating> findForCategoryAndSubjectIdSelector(Select<Record1<Long>> subjectIdSelector, long categoryId) {
        return mkExtendedBaseQuery()
                .where(dsl.renderInlined(MEASURABLE_CATEGORY.ID.eq(categoryId)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                                .and(MEASURABLE_RATING.ENTITY_ID.in(subjectIdSelector)))))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<MeasurableRating> findForCategoryAndMeasurableRatingIdSelector(Select<Record1<Long>> ratingIdSelector, long categoryId) {
        SelectConditionStep<Record> q = mkExtendedBaseQuery()
                .where(dsl.renderInlined(MEASURABLE_CATEGORY.ID.eq(categoryId)
                        .and(MEASURABLE_RATING.ID.in(ratingIdSelector))));
        return q
                .fetch(TO_DOMAIN_MAPPER);
    }

    public MeasurableRating getById(long id) {
        return mkBaseQuery()
                .where(MEASURABLE_RATING.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }



    public MeasurableRating getByDecommId(long decommId) {
        return mkBaseQuery()
                .innerJoin(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .on(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(decommId))
                .fetchOne(TO_DOMAIN_MAPPER);
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


    public List<MeasurableRatingTally> statsByMeasurableRatingIdSelector(Select<Record1<Long>> ratingIdSelector,
                                                                         boolean primaryOnly) {
        Condition cond = MEASURABLE_CATEGORY.ALLOW_PRIMARY_RATINGS.isFalse()
                .or(primaryOnly
                        ? MEASURABLE_RATING.IS_PRIMARY.isTrue()
                        : DSL.trueCondition());

        SelectHavingStep<Record3<Long, String, Integer>> q = dsl
                .select(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.RATING, DSL.count())
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE).on(MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .innerJoin(MEASURABLE_CATEGORY).on(MEASURABLE_CATEGORY.ID.eq(MEASURABLE.MEASURABLE_CATEGORY_ID))
                .where(dsl.renderInlined(MEASURABLE_RATING.ID.in(ratingIdSelector)
                        .and(cond)))
                .groupBy(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.RATING);

        return q
                .fetch(TO_TALLY_MAPPER);
    }


    public int deleteByMeasurableIdSelector(Select<Record1<Long>> selector) {
        return dsl
                .deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(selector))
                .execute();
    }


    public int removeForCategory(EntityReference ref, long categoryId) {
        SelectConditionStep<Record1<Long>> relevantMeasurableIds = select(MEASURABLE.ID)
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

    /**
     * Given a measurable and a ratingIdSelector, will determine if any other measurables are mapped by apps
     * which map to this measurable.  This is used to provide functionality for features like: "apps that
     * do this function, also do these functions..."
     *
     * @param ratingIdSelector     set of ratingIds to consider, these will be mapped back to their entities
     * @return boolean indicating if there are implicitly related measurables
     */
    public boolean hasMeasurableRatings(Select<Record1<Long>> ratingIdSelector) {
        return dsl.fetchExists(ratingIdSelector);
    }


    // --- utils

    private SelectJoinStep<Record> mkBaseQuery() {
        return dsl
                .select(MEASURABLE_RATING.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_FIELD)
                .select(ENTITY_DESCRIPTION_FIELD)
                .select(ENTITY_EXT_ID_FIELD)
                .from(MEASURABLE_RATING);
    }


    private SelectJoinStep<Record> mkExtendedBaseQuery() {
        return dsl
                .select(MEASURABLE_RATING.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_FIELD)
                .select(ENTITY_DESCRIPTION_FIELD)
                .select(ENTITY_EXT_ID_FIELD)
                .select(RATING_SCHEME_ITEM.ID)
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE).on(MEASURABLE_RATING.MEASURABLE_ID.eq(MEASURABLE.ID))
                .innerJoin(MEASURABLE_CATEGORY).on(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                .innerJoin(RATING_SCHEME_ITEM).on(MEASURABLE_RATING.RATING.eq(RATING_SCHEME_ITEM.CODE)
                        .and(MEASURABLE_CATEGORY.RATING_SCHEME_ID.eq(RATING_SCHEME_ITEM.SCHEME_ID)));
    }


    public Set<Operation> calculateAmendedRatingOperations(Set<Operation> operationsForEntityAssessment,
                                                           EntityReference entityReference,
                                                           long measurableId,
                                                           String username) {

        Field<Boolean> readOnlyRatingField = DSL.coalesce(MEASURABLE_RATING.IS_READONLY, DSL.val(false)).as("rating_read_only");

        SelectConditionStep<Record2<String, Boolean>> qry = dsl
                .select(USER_ROLE.ROLE,
                        readOnlyRatingField)
                .from(MEASURABLE)
                .leftJoin(MEASURABLE_CATEGORY).on(MEASURABLE_CATEGORY.ID.eq(Tables.MEASURABLE.MEASURABLE_CATEGORY_ID))
                .leftJoin(MEASURABLE_RATING).on(Tables.MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(entityReference.kind().name())
                                .and(MEASURABLE_RATING.ENTITY_ID.eq(entityReference.id()))))
                .leftJoin(USER_ROLE).on(USER_ROLE.ROLE.eq(MEASURABLE_CATEGORY.RATING_EDITOR_ROLE)
                        .and(USER_ROLE.USER_NAME.eq(username)))
                .where(MEASURABLE.ID.eq(measurableId));

        Tuple2<Boolean, Boolean> hasRoleAndIsReadOnly = qry
                .fetchOne(r -> tuple(
                        notEmpty(r.get(USER_ROLE.ROLE)),
                        r.get(readOnlyRatingField)));

        if (hasRoleAndIsReadOnly.v2) {
            return emptySet();
        } else if (hasRoleAndIsReadOnly.v1) {
            return union(operationsForEntityAssessment, asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));
        } else {
            return operationsForEntityAssessment;
        }
    }


    public Set<Operation> calculateAmendedAllocationOperations(Set<Operation> operationsForAllocation,
                                                               long categoryId,
                                                               String username) {

        boolean hasOverride = dsl
                .fetchExists(select(USER_ROLE.ROLE)
                        .from(MEASURABLE_CATEGORY)
                        .innerJoin(USER_ROLE).on(USER_ROLE.ROLE.eq(MEASURABLE_CATEGORY.RATING_EDITOR_ROLE)
                                .and(USER_ROLE.USER_NAME.eq(username)))
                        .where(MEASURABLE_CATEGORY.ID.eq(categoryId)));

        if (hasOverride) {
            return union(operationsForAllocation, asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));
        } else {
            return operationsForAllocation;
        }
    }


    /**
     * Takes a source measurable and will move all ratings, decommission dates, replacement applications and allocations to the target measurable where possible.
     * If a value already exists on the target the migration is ignored, or in the case of allocations, aggregated.
     *
     * @param measurableId         the source measurable from which to migrate data
     * @param targetId             the target measurable to inherit the data (where possible)
     * @param userId               the user responsible for the change
     * @param ratingSchemeId
     * @param ratingSchemeItems
     */
    public void migrateRatings(Long measurableId,
                               Long targetId,
                               String userId,
                               long ratingSchemeId,
                               List<RatingSchemeItem> ratingSchemeItems) {

        if (targetId == null) {
            throw new IllegalArgumentException("Cannot migrate ratings without specifying a new target");
        }

        LOG.info("Migrating ratings from measurable: {} to {}",
                measurableId,
                targetId);

        int sharedRatingCount = getSharedRatingsCount(measurableId, targetId);
        int sharedDecomCount = getSharedDecommsCount(measurableId, targetId);

        dsl.transaction(ctx -> {

            DSLContext tx = ctx.dsl();

            // RATINGS

            // direct allowable migration
            SelectOrderByStep<Record2<Long, String>> allowableMigrations = selectRatingsThatCanBeModified(measurableId, targetId);

            //target having ratings
            List<Long> entityIdList = getSharedRatingsEntityIds(measurableId, targetId);

            SelectConditionStep<Record9<Long, String, Long, String, String, Timestamp, String, String, Boolean>> ratingsToInsert = select(Tables.MEASURABLE_RATING.ENTITY_ID,
                    Tables.MEASURABLE_RATING.ENTITY_KIND,
                    DSL.val(targetId),
                    Tables.MEASURABLE_RATING.RATING,
                    Tables.MEASURABLE_RATING.DESCRIPTION,
                    Tables.MEASURABLE_RATING.LAST_UPDATED_AT,
                    Tables.MEASURABLE_RATING.LAST_UPDATED_BY,
                    Tables.MEASURABLE_RATING.PROVENANCE,
                    Tables.MEASURABLE_RATING.IS_READONLY)
                    .from(Tables.MEASURABLE_RATING)
                    .innerJoin(allowableMigrations).on(Tables.MEASURABLE_RATING.ENTITY_ID.eq(allowableMigrations.field(Tables.MEASURABLE_RATING.ENTITY_ID))
                            .and(Tables.MEASURABLE_RATING.ENTITY_KIND.eq(allowableMigrations.field(Tables.MEASURABLE_RATING.ENTITY_KIND))))
                    .where(Tables.MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId));

            int migratedRatings = tx
                    .insertInto(Tables.MEASURABLE_RATING)
                    .columns(Tables.MEASURABLE_RATING.ENTITY_ID,
                            Tables.MEASURABLE_RATING.ENTITY_KIND,
                            Tables.MEASURABLE_RATING.MEASURABLE_ID,
                            Tables.MEASURABLE_RATING.RATING,
                            Tables.MEASURABLE_RATING.DESCRIPTION,
                            Tables.MEASURABLE_RATING.LAST_UPDATED_AT,
                            Tables.MEASURABLE_RATING.LAST_UPDATED_BY,
                            Tables.MEASURABLE_RATING.PROVENANCE,
                            Tables.MEASURABLE_RATING.IS_READONLY)
                    .select(ratingsToInsert).execute();

            if (migratedRatings > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.MEASURABLE_RATING,
                        Operation.UPDATE,
                        format("Migrated %d ratings from measurable: %d to %d", migratedRatings, measurableId, targetId),
                        userId);
            }

            if (sharedRatingCount > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.MEASURABLE_RATING,
                        Operation.UPDATE,
                        format("Validation of rating precedence required for %d ratings from measurable: %d to %d due to existing ratings on the target", sharedRatingCount, measurableId, targetId),
                        userId);
            }

            // DECOMMS

            // all ratings that can be migrated, decomms automatically updated.
            // Need to find the ratings that cannot be migrated, then see if there is a target decom to match

            org.finos.waltz.schema.tables.MeasurableRating srcMr = MEASURABLE_RATING.as("srcMr");
            org.finos.waltz.schema.tables.MeasurableRating trgMr = MEASURABLE_RATING.as("trgMr");

            org.finos.waltz.schema.tables.MeasurableRatingPlannedDecommission srcDecom = MEASURABLE_RATING_PLANNED_DECOMMISSION.as("srcDecom");
            org.finos.waltz.schema.tables.MeasurableRatingPlannedDecommission trgDecom = MEASURABLE_RATING_PLANNED_DECOMMISSION.as("trgDecom");

            Field<Long> srcDecomId = srcDecom.ID.as("srcDecomId");
            Field<Long> trgRatingId = trgMr.ID.as("trgRatingId");

            SelectConditionStep<Record2<Long, Long>> decomsToUpdate = select(srcDecomId, trgRatingId)
                    .from(srcDecom)
                    .innerJoin(srcMr).on(srcDecom.MEASURABLE_RATING_ID.eq(srcMr.ID).and(srcMr.MEASURABLE_ID.eq(measurableId)))
                    .innerJoin(trgMr).on(srcMr.ENTITY_KIND.eq(trgMr.ENTITY_KIND).and(srcMr.ENTITY_ID.eq(trgMr.ENTITY_ID).and(trgMr.MEASURABLE_ID.eq(targetId))))
                    .leftJoin(trgDecom).on(trgMr.ID.eq(trgDecom.MEASURABLE_RATING_ID))
                    .where(trgDecom.ID.isNull());

            UpdateConditionStep<MeasurableRatingPlannedDecommissionRecord> updateDecomQry = tx
                    .update(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                    .set(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_RATING_ID, decomsToUpdate.field(trgRatingId))
                    .from(decomsToUpdate)
                    .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(decomsToUpdate.field(srcDecomId)));

            int migratedDecoms = updateDecomQry.execute();

            if (migratedDecoms > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION,
                        Operation.UPDATE,
                        format("Migrated %d decomms from measurable: %d to %d", migratedDecoms, measurableId, targetId),
                        userId);
            }

            if (sharedDecomCount > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION,
                        Operation.REMOVE,
                        format("Failed to migrate %d decomms from measurable: %d to %d due to existing decomms on the target", sharedDecomCount, measurableId, targetId),
                        userId);
            }


            // ALLOCATIONS


            // Either automatically moved if measurable rating was moved,
            // Migrated if existing measurable but no allocation
            // Aggregated is existing allocation on target measurable

            org.finos.waltz.schema.tables.Allocation srcAlloc = ALLOCATION.as("srcAlloc");
            org.finos.waltz.schema.tables.Allocation trgAlloc = ALLOCATION.as("trgAlloc");

            //get the list of all measurable ratings of target Id
            List<Record1<Long>> existingTargetRatingIds = tx.select(MEASURABLE_RATING.ID)
                    .from(MEASURABLE_RATING)
                    .where(MEASURABLE_RATING.MEASURABLE_ID.eq(targetId))
                    .fetch();

            List<Query> insertQueries = new ArrayList<>();

            List<Integer> allocList = existingTargetRatingIds.stream().map(rId -> {

                Long schemeIdWhenSrcAllocExists = tx.select(srcAlloc.ALLOCATION_SCHEME_ID)
                        .from(srcAlloc)
                        .join(srcMr)
                        .on(srcAlloc.MEASURABLE_RATING_ID.eq(srcMr.ID))
                        .join(trgMr)
                        .on(srcMr.ENTITY_ID.eq(trgMr.ENTITY_ID))
                        .where(srcMr.MEASURABLE_ID.eq(measurableId))
                        .and(trgMr.ID.eq(rId.value1()))
                        .fetchOne(srcAlloc.ALLOCATION_SCHEME_ID);

                if (null != schemeIdWhenSrcAllocExists) { //insert if source allocation exists
                    AllocationRecord record = tx.newRecord(ALLOCATION);
                    record.setAllocationSchemeId(schemeIdWhenSrcAllocExists);
                    record.setMeasurableRatingId(rId.value1());
                    record.setAllocationPercentage(0);
                    record.setLastUpdatedBy(userId);
                    record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    record.setProvenance("waltz");

                    insertQueries.add(tx.insertInto(ALLOCATION)
                            .set(record)
                            .onDuplicateKeyIgnore()); // This handles the "if not exists" logic
                    return 1;
                } else {
                    return 0;
                }
            }).collect(Collectors.toList());

            LOG.info("allocList : {}", allocList);

            if (!insertQueries.isEmpty()) {
                int insertedCount = tx.batch(insertQueries).execute().length;
                LOG.info("insertedCount : {}", insertedCount);
            }

            Field<Integer> totalAlloc = srcAlloc.ALLOCATION_PERCENTAGE.add(trgAlloc.ALLOCATION_PERCENTAGE).as("totalAlloc");
            Field<Long> updateTargetAllocId = trgAlloc.ID.as("updateTargetAllocId");

            SelectConditionStep<Record2<Long, Integer>> allocsToUpdate = select(updateTargetAllocId, totalAlloc)
                    .from(srcAlloc)
                    .innerJoin(srcMr).on(srcAlloc.MEASURABLE_RATING_ID.eq(srcMr.ID).and(srcMr.MEASURABLE_ID.eq(measurableId)))
                    .innerJoin(trgMr).on(srcMr.ENTITY_KIND.eq(trgMr.ENTITY_KIND).and(srcMr.ENTITY_ID.eq(trgMr.ENTITY_ID).and(trgMr.MEASURABLE_ID.eq(targetId))))
                    .leftJoin(trgAlloc).on(trgMr.ID.eq(trgAlloc.MEASURABLE_RATING_ID))
                    .where(trgAlloc.ID.isNotNull());

            Field<Long> migrateTargetAllocId = trgAlloc.ID.as("migrateTargetAllocId");
            Field<Integer> allocPercentage = srcAlloc.ALLOCATION_PERCENTAGE.as("allocPercentage");

            SelectConditionStep<Record2<Long, Integer>> allocsToMigrate = select(migrateTargetAllocId, allocPercentage)
                    .from(srcAlloc)
                    .innerJoin(srcMr).on(srcAlloc.MEASURABLE_RATING_ID.eq(srcMr.ID).and(srcMr.MEASURABLE_ID.eq(measurableId)))
                    .innerJoin(trgMr).on(srcMr.ENTITY_KIND.eq(trgMr.ENTITY_KIND).and(srcMr.ENTITY_ID.eq(trgMr.ENTITY_ID).and(trgMr.MEASURABLE_ID.eq(targetId))))
                    .leftJoin(trgAlloc).on(trgMr.ID.eq(trgAlloc.MEASURABLE_RATING_ID))
                    .where(trgAlloc.ID.isNull());

            UpdateConditionStep<AllocationRecord> mergeAllocQry = tx
                    .update(ALLOCATION)
                    .set(ALLOCATION.ALLOCATION_PERCENTAGE, totalAlloc)
                    .from(allocsToUpdate)
                    .where(ALLOCATION.ID.eq(allocsToUpdate.field(updateTargetAllocId)));

            int mergedAllocs = mergeAllocQry
                    .execute();

            UpdateConditionStep<AllocationRecord> updateAllocQry = tx
                    .update(ALLOCATION)
                    .set(ALLOCATION.ALLOCATION_PERCENTAGE, allocsToMigrate.field(srcAlloc.ALLOCATION_PERCENTAGE))
                    .from(allocsToMigrate)
                    .where(ALLOCATION.ID.eq(allocsToMigrate.field(migrateTargetAllocId)));

            int migratedAllocs = updateAllocQry
                    .execute();

            if (migratedAllocs > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.ALLOCATION,
                        Operation.UPDATE,
                        format("Migrated %d allocations from measurable: %d to %d", migratedAllocs, measurableId, targetId),
                        userId);
            }

            if (mergedAllocs > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.ALLOCATION,
                        Operation.UPDATE,
                        format("Merged %d allocations from measurable: %d to %d where there was an existing allocation on the target", mergedAllocs, measurableId, targetId),
                        userId);
            }

            LOG.info(format("Migrated %d ratings, %d decomms, %d/%d allocations (migrated/merged) from measurable: %d to %d",
                    migratedRatings,
                    migratedDecoms,
                    migratedAllocs,
                    mergedAllocs,
                    measurableId,
                    targetId));

            //update primary flag from source to target
            SelectConditionStep<Record1<Long>> ratingsToUpdateForPrimaryFlag = select(Tables.MEASURABLE_RATING.ENTITY_ID)
                    .from(Tables.MEASURABLE_RATING)
                    .where(Tables.MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId)
                            .and(Tables.MEASURABLE_RATING.IS_PRIMARY.isTrue()));

            int migratedPrimary = tx.update(Tables.MEASURABLE_RATING)
                    .set(Tables.MEASURABLE_RATING.IS_PRIMARY, Boolean.TRUE)
                    .where(Tables.MEASURABLE_RATING.ENTITY_ID.in(ratingsToUpdateForPrimaryFlag))
                    .and(Tables.MEASURABLE_RATING.MEASURABLE_ID.eq(targetId))
                    .execute();

            LOG.info("Migrated primary flag for intersecting rating, {}", migratedPrimary);

            //migrate overlapping measurable ratings
            //get source entries
            Map<Long, Integer> sourceEntries = getMappedEntries(measurableId, tx, entityIdList, ratingSchemeId);

            //get target entries
            Map<Long, Integer> targetEntries = getMappedEntries(targetId, tx, entityIdList, ratingSchemeId);

            //collect the entities with higher rating
            Map<Long, Integer> filteredEntities = targetEntries.entrySet().stream()
                    .filter(targetEntry -> {
                        Integer sourceValue = sourceEntries.get(targetEntry.getKey());
                        return sourceValue != null && sourceValue < targetEntry.getValue();
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, targetEntry -> sourceEntries.get(targetEntry.getKey())));

            if (null != filteredEntities && filteredEntities.size() > 0) {

                Map<Integer, String> ratingCodeMap = ratingSchemeItems.stream()
                        .collect(Collectors.toMap(
                                RatingSchemeItem::position,
                                RatingSchemeItem::rating,
                                (existingValue, newValue) -> existingValue // ignores the new value
                        ));

                //update rating with higher precedence
                List<Query> updateQueries = filteredEntities.entrySet().stream().map(r ->
                        tx.update(Tables.MEASURABLE_RATING)
                                .set(Tables.MEASURABLE_RATING.RATING, ratingCodeMap.get(r.getValue()))
                                .where(Tables.MEASURABLE_RATING.ENTITY_ID.eq(r.getKey()))
                                .and(Tables.MEASURABLE_RATING.MEASURABLE_ID.eq(targetId))
                ).collect(Collectors.toList());

                if (!updateQueries.isEmpty() && updateQueries.size() > 0) {
                    int updatedCount = tx.batch(updateQueries).execute().length;
                    LOG.info("ratings updatedCount : {}", updatedCount);
                }
            }
            int removedRatings = tx
                    .deleteFrom(Tables.MEASURABLE_RATING)
                    .where(Tables.MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId))
                    .execute();

            if (removedRatings > 0) {
                writeChangeLogForMerge(
                        tx,
                        targetId,
                        EntityKind.ALLOCATION,
                        Operation.REMOVE,
                        format("Removed %d ratings from measurable: with source measurable id as %d, migrated to target measurable id (%d)",
                                removedRatings,
                                measurableId,
                                targetId),
                        userId);
            }

            // allocations, decomms and replacements are automatically cleared up via cascade delete on fk
            LOG.info("Removed {} measurable ratings and any associated allocations, planned decommissions and replacement applications after migration", removedRatings);
        });
    }

    private static Map<Long, Integer> getMappedEntries(Long id, DSLContext tx, List<Long> entityIdList, long ratingSchemeId) {
        return tx
                .select(
                        MEASURABLE_RATING.ENTITY_ID,
                        RATING_SCHEME_ITEM.POSITION
                )
                .from(MEASURABLE_RATING)
                .leftJoin(RATING_SCHEME_ITEM)
                .on(MEASURABLE_RATING.RATING.eq(RATING_SCHEME_ITEM.CODE))
                .and(RATING_SCHEME_ITEM.SCHEME_ID.eq(ratingSchemeId))
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(id))
                .and(RATING_SCHEME_ITEM.POSITION.isNotNull())
                .and(MEASURABLE_RATING.ENTITY_ID.in(entityIdList))
                .fetchMap(MEASURABLE_RATING.ENTITY_ID, RATING_SCHEME_ITEM.POSITION);
    }

    private List<Long> getSharedRatingsEntityIds(Long measurableId, Long targetId) {
        SelectOrderByStep<Record2<Long, String>> sharedRatings = getSharedRatings(measurableId, targetId);
        return dsl.fetch(sharedRatings).getValues(MEASURABLE_RATING.ENTITY_ID);
    }

    private void writeChangeLogForMerge(DSLContext tx, Long measurableId, EntityKind childKind, Operation operation, String message, String userId) {
        tx
                .insertInto(CHANGE_LOG)
                .columns(CHANGE_LOG.PARENT_KIND,
                        CHANGE_LOG.PARENT_ID,
                        CHANGE_LOG.MESSAGE,
                        CHANGE_LOG.USER_ID,
                        CHANGE_LOG.SEVERITY,
                        CHANGE_LOG.CREATED_AT,
                        CHANGE_LOG.CHILD_KIND,
                        CHANGE_LOG.OPERATION)
                .values(EntityKind.MEASURABLE.name(),
                        measurableId,
                        message,
                        userId,
                        Severity.INFORMATION.name(),
                        DateTimeUtilities.nowUtcTimestamp(),
                        childKind.name(),
                        operation.name())
                .execute();
    }


    private SelectOrderByStep<Record2<Long, String>> selectRatingsThatCanBeModified(Long measurableId, Long targetId) {

        SelectConditionStep<Record2<Long, String>> targets = mkEntitySelectForMeasurable(targetId);
        SelectConditionStep<Record2<Long, String>> migrations = mkEntitySelectForMeasurable(measurableId);

        return migrations.except(targets);
    }


    private SelectConditionStep<Record2<Long, String>> mkEntitySelectForMeasurable(Long measurableId) {
        return select(Tables.MEASURABLE_RATING.ENTITY_ID, Tables.MEASURABLE_RATING.ENTITY_KIND)
                .from(Tables.MEASURABLE_RATING)
                .where(Tables.MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId));
    }


    public int getSharedRatingsCount(Long measurableId, Long targetId) {
        SelectOrderByStep<Record2<Long, String>> sharedRatings = getSharedRatings(measurableId, targetId);
        return dsl.fetchCount(sharedRatings);
    }

    private SelectOrderByStep<Record2<Long, String>> getSharedRatings(Long measurableId, Long targetId) {
        SelectConditionStep<Record2<Long, String>> targets = mkEntitySelectForMeasurable(targetId);
        SelectConditionStep<Record2<Long, String>> migrations = mkEntitySelectForMeasurable(measurableId);
        return migrations.intersect(targets);
    }

    public int getSharedDecommsCount(Long measurableId, Long targetId) {

        SelectConditionStep<Record2<Long, String>> targets = mkEntitySelectForDecomm(targetId);
        SelectConditionStep<Record2<Long, String>> migrations = mkEntitySelectForDecomm(measurableId);

        SelectOrderByStep<Record2<Long, String>> sharedDecomms = migrations.intersect(targets);

        return dsl.fetchCount(sharedDecomms);
    }

    private SelectConditionStep<Record2<Long, String>> mkEntitySelectForDecomm(Long measurableId) {
        return select(MEASURABLE_RATING.ENTITY_ID, MEASURABLE_RATING.ENTITY_KIND)
                .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .innerJoin(MEASURABLE_RATING).on(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId));
    }


    public boolean saveRatingItem(EntityReference entityRef,
                                  long measurableId,
                                  String ratingCode,
                                  String username) {
        return MeasurableRatingUtilities.saveRatingItem(
                dsl,
                entityRef,
                measurableId,
                ratingCode,
                username);
    }


    public boolean saveRatingIsPrimary(EntityReference entityRef, long measurableId, boolean isPrimary, String username) {
        return dsl.transactionResult(ctx -> MeasurableRatingUtilities.saveRatingIsPrimary(
                ctx.dsl(),
                entityRef,
                measurableId,
                isPrimary,
                username));
    }


    public boolean saveRatingDescription(EntityReference entityRef, long measurableId, String description, String username) {
        return dsl.transactionResult(ctx -> MeasurableRatingUtilities.saveRatingDescription(
                ctx.dsl(),
                entityRef,
                measurableId,
                description,
                username));
    }


    public MeasurableRatingChangeSummary resolveLoggingContextForRatingChange(EntityReference entityRef,
                                                                              long measurableId,
                                                                              String desiredRatingCode) {
        return MeasurableRatingUtilities.resolveLoggingContextForRatingChange(dsl, entityRef, measurableId, desiredRatingCode);
    }

    /*
     * Should move to using a measurable rating id selector
     */
    @Deprecated
    public Set<MeasurableRating> findPrimaryRatingsForGenericSelector(GenericSelector selector) {
        return mkExtendedBaseQuery()
                .where(dsl.renderInlined(MEASURABLE_RATING.ENTITY_KIND.eq(selector.kind().name())
                        .and(MEASURABLE_RATING.ENTITY_ID.in(selector.selector()))
                        .and(MEASURABLE_RATING.IS_PRIMARY.isTrue())))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<MeasurableRating> findPrimaryRatingsForMeasurableIdSelector(Select<Record1<Long>> ratingIdSelector) {
        return mkExtendedBaseQuery()
                .where(dsl.renderInlined(MEASURABLE_RATING.ID.in(ratingIdSelector)
                        .and(MEASURABLE_RATING.IS_PRIMARY.isTrue())))
                .fetchSet(TO_DOMAIN_MAPPER);
    }
}
