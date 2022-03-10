package org.finos.waltz.service.permission;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.permission_group.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class PermissionGroupDao {

    private final DSLContext dsl;


    private static final Set<Long> ALL_USERS_ALLOWED = asSet((Long) null);


    public PermissionGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<Permission> getPermissionsForEntityRef(EntityReference parentEntityRef) {
        return getPermissionsForEntityRef(parentEntityRef, DSL.trueCondition());
    }


    public Set<Permission> findPermissionsForEntityRefAndSubjectKind(EntityReference parentEntityRef, EntityKind subjectKind) {
        return getPermissionsForEntityRef(parentEntityRef, PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(subjectKind.name()));
    }


    public Set<Permission> getPermissionsForEntityRef(EntityReference parentEntityRef, Condition condition) {

        Condition groupCondition = PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID.in(permissionGroupSelector(parentEntityRef))
                .or(PERMISSION_GROUP.IS_DEFAULT.isTrue());

        Map<Tuple3<String, String, Long>, List<Long>> permissionsForSubjectQualifier = dsl
                .select(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                        PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                        PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                        INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .innerJoin(PERMISSION_GROUP).on(PERMISSION_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .leftJoin(INVOLVEMENT_GROUP).on(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP.ID))
                .leftJoin(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT_GROUP.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .where(groupCondition)
                .and(condition)
                .fetchGroups(
                        r -> tuple(
                                r.get(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID)),
                        r -> r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID));

        return permissionsForSubjectQualifier
                .entrySet()
                .stream()
                .map(e -> {
                    Tuple3<String, String, Long> groupInfo = e.getKey();
                    Set<Long> requiredInvolvementIds = fromCollection(e.getValue());

                    ImmutableRequiredInvolvementsResult requiredInvolvementsResult = ImmutableRequiredInvolvementsResult
                            .builder()
                            .areAllUsersAllowed(requiredInvolvementIds.equals(ALL_USERS_ALLOWED))
                            .requiredInvolvementKindIds(minus(requiredInvolvementIds, ALL_USERS_ALLOWED))
                            .build();

                    String qualifierKind = groupInfo.v2;

                    return ImmutablePermission.builder()
                            .subjectKind(EntityKind.valueOf(groupInfo.v1))
                            .qualifierKind(qualifierKind == null ? null : EntityKind.valueOf(qualifierKind))
                            .qualifierId(Optional.ofNullable(groupInfo.v3))
                            .requiredInvolvementsResult(requiredInvolvementsResult)
                            .build();
                })
                .collect(Collectors.toSet());
    }


    public Set<Permission> getDefaultPermissions() {

        Map<Tuple3<String, String, Long>, List<Long>> permissionsForSubjectQualifier = dsl
                .select(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                        PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                        PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                        INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .innerJoin(PERMISSION_GROUP).on(PERMISSION_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .leftJoin(INVOLVEMENT_GROUP).on(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP.ID))
                .leftJoin(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT_GROUP.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .where(PERMISSION_GROUP.IS_DEFAULT.isTrue())
                .fetchGroups(
                        r -> tuple(
                                r.get(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND),
                                r.get(PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID)),
                        r -> r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID));

        return permissionsForSubjectQualifier
                .entrySet()
                .stream()
                .map(e -> {
                    Tuple3<String, String, Long> groupInfo = e.getKey();
                    Set<Long> requiredInvolvementIds = fromCollection(e.getValue());

                    ImmutableRequiredInvolvementsResult requiredInvolvementsResult = ImmutableRequiredInvolvementsResult
                            .builder()
                            .areAllUsersAllowed(requiredInvolvementIds.equals(ALL_USERS_ALLOWED))
                            .requiredInvolvementKindIds(minus(requiredInvolvementIds, ALL_USERS_ALLOWED))
                            .build();

                    return ImmutablePermission.builder()
                            .subjectKind(EntityKind.valueOf(groupInfo.v1))
                            .qualifierKind(EntityKind.valueOf(groupInfo.v2))
                            .qualifierId(Optional.ofNullable(groupInfo.v3))
                            .requiredInvolvementsResult(requiredInvolvementsResult)
                            .build();
                })
                .collect(Collectors.toSet());
    }


    private SelectConditionStep<Record1<Long>> permissionGroupSelector(EntityReference parentEntityRef) {
        switch (parentEntityRef.kind()) {
            case APPLICATION:
                return DSL
                        .select(PERMISSION_GROUP_ENTRY.PERMISSION_GROUP_ID)
                        .from(PERMISSION_GROUP_ENTRY)
                        .where(PERMISSION_GROUP_ENTRY.APPLICATION_ID.eq(parentEntityRef.id()));
            default:
                throw new UnsupportedOperationException("Cannot find permission group for kind: " + parentEntityRef.kind());

        }
    }


    public RequiredInvolvementsResult getRequiredInvolvements(CheckPermissionCommand permissionCommand) {

        Condition groupCondition = PERMISSION_GROUP.ID.in(permissionGroupSelector(permissionCommand.parentEntityRef()))
                .or(PERMISSION_GROUP.IS_DEFAULT.isTrue());

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
                .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(permissionCommand.subjectKind().name()))
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


    public Set<Long> findExistingInvolvementKindIdsForUser(EntityReference parentEntityRef, String username) {
        return dsl
                .select(INVOLVEMENT.KIND_ID)
                .from(INVOLVEMENT)
                .innerJoin(PERSON).on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(PERSON.EMAIL.eq(username)
                        .and(INVOLVEMENT.ENTITY_KIND.eq(parentEntityRef.kind().name()))
                        .and(INVOLVEMENT.ENTITY_ID.eq(parentEntityRef.id())))
                .fetchSet(INVOLVEMENT.KIND_ID);
    }
}
