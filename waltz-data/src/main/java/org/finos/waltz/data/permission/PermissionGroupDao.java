package org.finos.waltz.data.permission;

import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.attestation.ImmutableUserAttestationPermission;
import org.finos.waltz.model.attestation.UserAttestationPermission;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.permission_group.*;
import org.finos.waltz.schema.Tables;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple5;
import org.springframework.stereotype.Repository;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.groupAndThen;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class PermissionGroupDao {

    private static final Set<Long> ALL_USERS_ALLOWED = asSet((Long) null); // the set containing the value <null>

    private final DSLContext dsl;


    public PermissionGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<Permission> findPermissionsForParentEntityReference(EntityReference parentEntityRef) {

        Condition groupCondition = PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID.in(permissionGroupSelector(parentEntityRef))
                .or(PERMISSION_GROUP.IS_DEFAULT.isTrue()
                        .and(PERMISSION_GROUP.ID.notIn(permissionGroupSelector(parentEntityRef))));

        Map<Tuple5<String, String, Long, String, String>, List<Long>> permissionsForSubjectQualifier = dsl
                .select(PERMISSION_GROUP_INVOLVEMENT.OPERATION,
                        PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                        PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                        PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND,
                        PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                        INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .innerJoin(PERMISSION_GROUP).on(PERMISSION_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .leftJoin(INVOLVEMENT_GROUP).on(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP.ID))
                .leftJoin(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT_GROUP.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .where(groupCondition)
                .fetchGroups(
                        r -> tuple(
                                r.get(PERMISSION_GROUP_INVOLVEMENT.OPERATION),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND)),
                        r -> r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID));

        return permissionsForSubjectQualifier
                .entrySet()
                .stream()
                .map(e -> {
                    Tuple5<String, String, Long, String, String> groupInfo = e.getKey();
                    Set<Long> requiredInvolvementIds = fromCollection(e.getValue());

                    ImmutableRequiredInvolvementsResult requiredInvolvementsResult = ImmutableRequiredInvolvementsResult
                            .builder()
                            .areAllUsersAllowed(requiredInvolvementIds.equals(ALL_USERS_ALLOWED))
                            .requiredInvolvementKindIds(minus(requiredInvolvementIds, ALL_USERS_ALLOWED))
                            .build();

                    String qualifierKind = groupInfo.v2;
                    Long qualifierId = groupInfo.v3;


                    ImmutablePermission.Builder build = ImmutablePermission.builder()
                            .operation(Operation.valueOf(groupInfo.v1))
                            .parentKind(EntityKind.valueOf(groupInfo.v4))
                            .subjectKind(EntityKind.valueOf(groupInfo.v5))
                            .requiredInvolvementsResult(requiredInvolvementsResult);

                    if (qualifierId != null && qualifierKind != null) {
                        build.qualifierReference(mkRef(EntityKind.valueOf(qualifierKind), qualifierId));
                    }

                    return build.build();
                })
                .collect(toSet());
    }


    private SelectConditionStep<Record1<Long>> permissionGroupSelector(EntityReference parentEntityRef) {
        switch (parentEntityRef.kind()) {
            case APPLICATION:
            case CHANGE_INITIATIVE:
                return DSL
                        .select(PERMISSION_GROUP_ENTRY.PERMISSION_GROUP_ID)
                        .from(PERMISSION_GROUP_ENTRY)
                        .where(PERMISSION_GROUP_ENTRY.ENTITY_ID.eq(parentEntityRef.id())
                                .and(PERMISSION_GROUP_ENTRY.ENTITY_KIND.eq(parentEntityRef.kind().name())));
            default:
                throw new UnsupportedOperationException("Cannot find permission group for kind: " + parentEntityRef.kind());

        }
    }


    public RequiredInvolvementsResult getRequiredInvolvements(CheckPermissionCommand permissionCommand) {

        Condition groupCondition = PERMISSION_GROUP.ID.in(permissionGroupSelector(permissionCommand.parentEntityRef()))
                .or(PERMISSION_GROUP.IS_DEFAULT.isTrue()
                        .and(PERMISSION_GROUP.ID.notIn(permissionGroupSelector(permissionCommand.parentEntityRef()))));

        Condition qualifierKindCondition = permissionCommand.qualifierKind() == null
                ? PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND.isNull()
                : PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND.eq(permissionCommand.qualifierKind().name());

        Condition qualifierIdCondition = permissionCommand.qualifierId() == null
                ? PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID.isNull()
                : PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID.eq(permissionCommand.qualifierId());

        SelectConditionStep<Record1<Long>> qry = dsl
                .select(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .innerJoin(PERMISSION_GROUP).on(PERMISSION_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .leftJoin(INVOLVEMENT_GROUP).on(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP.ID))
                .leftJoin(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT_GROUP.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .where(groupCondition)
                .and(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND.eq(permissionCommand.parentEntityRef().kind().name()))
                .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(permissionCommand.subjectKind().name()))
                .and(PERMISSION_GROUP_INVOLVEMENT.OPERATION.eq(permissionCommand.operation().name()))
                .and(qualifierKindCondition)
                .and(qualifierIdCondition);

        Set<Long> requiredInvolvementIds = qry
                .fetchSet(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID);

        return ImmutableRequiredInvolvementsResult
                .builder()
                .areAllUsersAllowed(requiredInvolvementIds.equals(ALL_USERS_ALLOWED))
                .requiredInvolvementKindIds(minus(requiredInvolvementIds, ALL_USERS_ALLOWED))
                .build();
    }


    /**
     * Given an entity ref (typically an app) and a userId, this will return a list of
     * measurable categories that are attestable along with a flag to indicate if _this_
     * user can attest the category for the entity.
     * @param ref  the entity being attested
     * @param userId  the user who _may_ be doing the attestation
     * @return  a set of tuples: [{category, userCanAttestForThisEntityFlag}, ...]
     */
    public Set<UserAttestationPermission> findSupportedMeasurableCategoryAttestations(EntityReference ref, String userId) {
        // specific involvements only exist for apps,
        // ... therefore if the given ref is not an app we want to 'force-fail' the (upcoming) left join
        Condition specificApplicationPermissionGroupEntryJoinCondition = ref.kind() == EntityKind.APPLICATION
                ? PERMISSION_GROUP_ENTRY.PERMISSION_GROUP_ID.eq(PERMISSION_GROUP.ID)
                .and(PERMISSION_GROUP_ENTRY.ENTITY_ID.eq(ref.id()))
                : DSL.falseCondition();

        SelectConditionStep<Record> qry = dsl
                .select(MEASURABLE_CATEGORY.fields())
                .select(PERMISSION_GROUP.IS_DEFAULT)
                .select(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .innerJoin(PERMISSION_GROUP).on(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID.eq(PERMISSION_GROUP.ID))
                .innerJoin(INVOLVEMENT_GROUP).on(INVOLVEMENT_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID))
                .innerJoin(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT_GROUP.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .innerJoin(MEASURABLE_CATEGORY).on(MEASURABLE_CATEGORY.ID.eq(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID)
                        .and(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND.eq(EntityKind.MEASURABLE_CATEGORY.name())))
                .leftJoin(PERMISSION_GROUP_ENTRY).on(specificApplicationPermissionGroupEntryJoinCondition)
                .where(PERMISSION_GROUP_INVOLVEMENT.OPERATION.eq(Operation.ATTEST.name()))
                .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(EntityKind.MEASURABLE_RATING.name()))
                .and(PERMISSION_GROUP.IS_DEFAULT.isTrue()
                        .or(PERMISSION_GROUP_ENTRY.PERMISSION_GROUP_ID.isNotNull()));

        Set<Tuple3<MeasurableCategory, Boolean, Long>> data = qry
                .fetchSet(r -> tuple(
                    MeasurableCategoryDao.TO_DOMAIN_MAPPER.map(r),
                    r.get(PERMISSION_GROUP.IS_DEFAULT),
                    r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)));

        if (data.isEmpty()) {
            // no point continuing as no measurable categories are available for attestation, regardless of involvement
            return emptySet();
        }

        Map<MeasurableCategory, Collection<Long>> categoryByInvKindsNeeded = groupAndThen(
                data,
                d -> d.v1,  // grouping by measurable category
                xs -> {
                    // then, for each category, we group by `is_default` and take only the involvement kind ids
                    Map<Boolean, Collection<Long>> invKindsByDefault = groupBy(
                            xs,
                            t -> t.v2,   // is_default
                            t -> t.v3);  // involvement_kind

                    // check to see if there are any specific involvement kinds for this entity...
                    Collection<Long> specificInvKindsNeeded = invKindsByDefault.getOrDefault(
                            Boolean.FALSE,
                            emptySet());

                    return specificInvKindsNeeded.isEmpty()
                            ? invKindsByDefault.getOrDefault(   // ... if not, return the default involvements
                                    Boolean.TRUE,
                                    emptySet())
                            : specificInvKindsNeeded;  // ... if so return them
                });

        // We now want to see if _this_ user has those involvements
        // ...so first we get all the users involvement kinds for this entity
        Set<Long> existingInvolvementKinds = findExistingInvolvementKinds(ref, userId);

        // ...now we map over the categories and required involvements,
        // ...checking to see if the user's involvement satisfies each one
        return map(
                categoryByInvKindsNeeded.entrySet(),
                kv -> ImmutableUserAttestationPermission
                        .builder()
                        .subjectKind(EntityKind.MEASURABLE_RATING)
                        .parentKind(EntityKind.APPLICATION)
                        .qualifierReference(kv.getKey().entityReference())
                        .hasPermission(hasIntersection(
                                fromCollection(kv.getValue()),
                                existingInvolvementKinds))
                        .build());
    }


    private Set<Long> findExistingInvolvementKinds(EntityReference ref, String userId) {
        return dsl
                .select(Tables.INVOLVEMENT.KIND_ID)
                .from(Tables.INVOLVEMENT)
                .innerJoin(Tables.PERSON).on(Tables.PERSON.EMPLOYEE_ID.eq(Tables.INVOLVEMENT.EMPLOYEE_ID))
                .where(Tables.PERSON.EMAIL.eq(userId)
                        .and(Tables.INVOLVEMENT.ENTITY_KIND.eq(ref.kind().name()))
                        .and(Tables.INVOLVEMENT.ENTITY_ID.eq(ref.id())))
                .fetchSet(Tables.INVOLVEMENT.KIND_ID);
    }
}
