package org.finos.waltz.data.app_group;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_relationship.RelationshipKind;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.EntityRelationship;
import org.finos.waltz.schema.tables.records.ApplicationGroupEntryRecord;
import org.finos.waltz.schema.tables.records.ApplicationGroupOuEntryRecord;
import org.finos.waltz.schema.tables.records.EntityRelationshipRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;

public class AppGroupHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AppGroupHelper.class);
    private static final EntityRelationship er = Tables.ENTITY_RELATIONSHIP;

    /**
     * Adds and removes app group entries in bulk.
     *
     * @param dsl       database context to use
     * @param additions [tuple2{groupId, ref}]
     * @param removals  [tuple2{groupId, ref}]
     * @param userId    user id to set as the createdBy user
     * @return
     */
    public static int processAdditionsAndRemovals(DSLContext dsl,
                                                  Set<Tuple2<Long, EntityReference>> additions,
                                                  Set<Tuple2<Long, EntityReference>> removals,
                                                  String userId) {

        Map<EntityKind, Collection<Tuple2<Long, EntityReference>>> additionsByKind = groupBy(additions, t -> t.v2.kind());
        Map<EntityKind, Collection<Tuple2<Long, EntityReference>>> removalsByKind = groupBy(removals, t -> t.v2.kind());

        int appAdditions = handleEntries(additionsByKind, EntityKind.APPLICATION, xs -> handleAppAdditions(dsl, xs));
        int ouAdditions = handleEntries(additionsByKind, EntityKind.ORG_UNIT, xs -> handleOrgUnitAdditions(dsl, xs));
        int ciAdditions = handleEntries(additionsByKind, EntityKind.CHANGE_INITIATIVE, xs -> handleChangeInitiativeAdditions(dsl, xs, userId));

        int appRemovals = handleEntries(removalsByKind, EntityKind.APPLICATION, xs -> handleAppRemovals(dsl, xs));
        int ouRemovals = handleEntries(removalsByKind, EntityKind.ORG_UNIT, xs -> handleOrgUnitRemovals(dsl, xs));
        int ciRemovals = handleEntries(removalsByKind, EntityKind.CHANGE_INITIATIVE, xs -> handleChangeInitiativeRemovals(dsl, xs));

        int totalAdditions = appAdditions + ciAdditions + ouAdditions;
        int totalRemovals = appRemovals + ciRemovals + ouRemovals;

        int total = totalAdditions + totalRemovals;

        LOG.debug("Added - apps: {}, cis: {}, ous: {} - total: {}", appAdditions, ciAdditions, ouAdditions, totalAdditions);
        LOG.debug("Removed - apps: {}, cis: {}, ous: {} - total: {}", appRemovals, ciRemovals, ouRemovals, totalRemovals);
        LOG.debug("Total number of app group records changes: {}", total);

        return total;
    }


    private static Integer handleChangeInitiativeRemovals(DSLContext dsl,
                                                          Collection<Tuple2<Long, EntityReference>> xs) {
        return summarizeResults(xs
                .stream()
                .map(t -> dsl
                    .deleteFrom(er)
                    .where(er.KIND_A.eq(EntityKind.APP_GROUP.name())
                        .and(er.ID_A.eq(t.v1))
                        .and(er.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                        .and(er.ID_B.eq(t.v2.id()))
                        .and(er.RELATIONSHIP.eq(RelationshipKind.RELATES_TO.name()))))
                .collect(collectingAndThen(toSet(), dsl::batch))
                .execute());
    }


    private static int handleChangeInitiativeAdditions(DSLContext dsl,
                                                       Collection<Tuple2<Long, EntityReference>> xs,
                                                       String userId) {
        Timestamp now = DateTimeUtilities.nowUtcTimestamp();
        return summarizeResults(xs
                .stream()
                .map(t -> {
                    EntityRelationshipRecord r = dsl.newRecord(er);
                    r.setKindA(EntityKind.APP_GROUP.name());
                    r.setIdA(t.v1);
                    r.setKindB(EntityKind.CHANGE_INITIATIVE.name());
                    r.setIdB(t.v2.id());
                    r.setProvenance("waltz");
                    r.setLastUpdatedBy(userId);
                    r.setLastUpdatedAt(now);
                    r.setRelationship(RelationshipKind.RELATES_TO.name());
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchInsert))
                .execute());
    }


    private static int handleOrgUnitRemovals(DSLContext dsl,
                                             Collection<Tuple2<Long, EntityReference>> xs) {
        return summarizeResults(xs
                .stream()
                .map(t -> {
                    ApplicationGroupOuEntryRecord r = dsl.newRecord(Tables.APPLICATION_GROUP_OU_ENTRY);
                    r.setGroupId(t.v1);
                    r.setOrgUnitId(t.v2.id());
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchDelete))
                .execute());
    }


    private static int handleAppRemovals(DSLContext dsl,
                                         Collection<Tuple2<Long, EntityReference>> xs) {
        return summarizeResults(xs
                .stream()
                .map(t -> {
                    ApplicationGroupEntryRecord r = dsl.newRecord(Tables.APPLICATION_GROUP_ENTRY);
                    r.setGroupId(t.v1);
                    r.setApplicationId(t.v2.id());
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchDelete))
                .execute());
    }


    private static int handleOrgUnitAdditions(DSLContext dsl,
                                              Collection<Tuple2<Long, EntityReference>> xs) {
        return summarizeResults(xs
                .stream()
                .map(t -> {
                    ApplicationGroupOuEntryRecord r = dsl.newRecord(Tables.APPLICATION_GROUP_OU_ENTRY);
                    r.setGroupId(t.v1);
                    r.setOrgUnitId(t.v2.id());
                    r.setIsReadonly(true);
                    r.setProvenance("waltz");
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchInsert))
                .execute());
    }


    private static int handleAppAdditions(DSLContext dsl,
                                          Collection<Tuple2<Long, EntityReference>> xs) {
        return summarizeResults(xs
                .stream()
                .map(t -> {
                    ApplicationGroupEntryRecord r = dsl.newRecord(Tables.APPLICATION_GROUP_ENTRY);
                    r.setGroupId(t.v1);
                    r.setApplicationId(t.v2.id());
                    r.setIsReadonly(true);
                    r.setProvenance("waltz");
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchInsert))
                .execute());
    }


    private static int handleEntries(Map<EntityKind, Collection<Tuple2<Long, EntityReference>>> entriesByKind,
                                     EntityKind kind,
                                     Function<Collection<Tuple2<Long, EntityReference>>, Integer> handler) {
        Collection<Tuple2<Long, EntityReference>> xs = entriesByKind.getOrDefault(kind, Collections.emptySet());
        if (xs.isEmpty()) {
            return 0;
        } else {
            return handler.apply(xs);
        }
    }
}
