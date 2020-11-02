package com.khartec.waltz.service.permission;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.permission_group.ImmutablePermission;
import com.khartec.waltz.model.permission_group.Permission;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static com.khartec.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;

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

    public PermissionGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Permission> getPermissionsForEntityRef(EntityReference parentEntityRef) {
        return dsl.select(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                PERMISSION_GROUP.IS_DEFAULT
                )
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .join(PERMISSION_GROUP)
                .on(PERMISSION_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .where(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID.in(permissionGroupSelector(parentEntityRef)))
                .fetch(TO_MAPPER);
    }

    public List<Permission> getDefaultPermissions() {
        return dsl.select(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND,
                PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_KIND,
                PERMISSION_GROUP_INVOLVEMENT.QUALIFIER_ID,
                PERMISSION_GROUP.IS_DEFAULT
        )
                .from(PERMISSION_GROUP_INVOLVEMENT)
                .join(PERMISSION_GROUP)
                .on(PERMISSION_GROUP.ID.eq(PERMISSION_GROUP_INVOLVEMENT.PERMISSION_GROUP_ID))
                .where(PERMISSION_GROUP.IS_DEFAULT.eq(true))
                .fetch(TO_MAPPER);
    }

    private SelectConditionStep<Record1<Long>> permissionGroupSelector(EntityReference parentEntityRef) {
        switch(parentEntityRef.kind()) {
            case APPLICATION:
                return DSL
                        .select(PERMISSION_GROUP_ENTRY.PERMISSION_GROUP_ID)
                        .where(PERMISSION_GROUP_ENTRY.APPLICATION_ID.eq(parentEntityRef.id()));
            default:
                throw new UnsupportedOperationException("Cannot find permission group for kind: " + parentEntityRef.kind());

        }

    }
}
