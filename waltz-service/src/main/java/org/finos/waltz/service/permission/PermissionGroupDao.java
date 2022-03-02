package org.finos.waltz.service.permission;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.permission_group.*;
import org.finos.waltz.schema.tables.PermissionGroupInvolvement;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.minus;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;

@Repository
public class PermissionGroupDao {

    private final DSLContext dsl;

    private static final RecordMapper<Record4<String,String,Long,Boolean>, Permission> TO_MAPPER =
            record -> {
        String subjectKind = record.value1();
        String qualifierKind = record.value2();
        Long qualifierId = record.value3();
        boolean isDefault = record.value4();

        return ImmutablePermission.builder()
                .subjectKind(EntityKind.valueOf(subjectKind))
                .qualifierKind(EntityKind.valueOf(qualifierKind))
                .qualifierId(Optional.ofNullable(qualifierId))
                .isDefault(isDefault)
                .build();
    };

    private static final Set<Long> ALL_USERS_ALLOWED = SetUtilities.asSet((Long) null);


    public PermissionGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Permission> getPermissionsForEntityRef(EntityReference parentEntityRef) {
        return dsl.select(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                PERMISSION_GROUP.IS_DEFAULT
                )
                .from(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT)
                .join(PERMISSION_GROUP)
                .on(PERMISSION_GROUP.ID.eq(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .where(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID.in(permissionGroupSelector(parentEntityRef)))
                .fetch(TO_MAPPER);
    }

    public List<Permission> getDefaultPermissions() {
        return dsl
            .select(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                    PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                    PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                    PERMISSION_GROUP.IS_DEFAULT)
            .from(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT)
            .join(PERMISSION_GROUP)
            .on(PERMISSION_GROUP.ID.eq(PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
            .where(PERMISSION_GROUP.IS_DEFAULT.eq(true))
            .fetch(TO_MAPPER);
    }

    private SelectConditionStep<Record1<Long>> permissionGroupSelector(EntityReference parentEntityRef) {
        switch(parentEntityRef.kind()) {
            case APPLICATION:
                return DSL
                        .select(PERMISSION_GROUP_ENTRY.PERMISSION_GROUP_ID)
                        .from(PERMISSION_GROUP_ENTRY)
                        .where(PERMISSION_GROUP_ENTRY.APPLICATION_ID.eq(parentEntityRef.id()));
            default:
                throw new UnsupportedOperationException("Cannot find permission group for kind: " + parentEntityRef.kind());

        }
    }


    public RequiredInvolvementsResult findRequiredInvolvements(CheckPermissionCommand permissionCommand) {

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


    public Set<Long> findExistingInvolvementKindIdsForUser(CheckPermissionCommand checkCommand) {
        return dsl
                .select(INVOLVEMENT.KIND_ID)
                .from(INVOLVEMENT)
                .innerJoin(PERSON).on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(PERSON.EMAIL.eq(checkCommand.user()))
                .and(INVOLVEMENT.ENTITY_KIND.eq(checkCommand.parentEntityRef().kind().name()))
                .and(INVOLVEMENT.ENTITY_ID.eq(checkCommand.parentEntityRef().id()))
                .fetchSet(INVOLVEMENT.KIND_ID);
    }
}
