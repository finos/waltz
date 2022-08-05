package org.finos.waltz.test_common_again.helpers;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.schema.tables.records.*;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.tables.InvolvementGroup.INVOLVEMENT_GROUP;
import static org.finos.waltz.schema.tables.InvolvementGroupEntry.INVOLVEMENT_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
import static org.finos.waltz.test_common_again.helpers.NameHelper.mkName;

@Service
public class PermissionGroupHelper {

    @Autowired
    private DSLContext dsl;

    @Autowired
    public PermissionGroupHelper() {
    }


    public Long setupSpecificPermissionGroupForApp(EntityReference appRef, Long involvementKindId, String pgName) {
        InvolvementGroupRecord ig = setupInvolvementGroup(involvementKindId, pgName);
        PermissionGroupRecord pg = setupPermissionGroup(appRef, ig, pgName);
        return pg.getId();
    }


    public PermissionGroupRecord setupPermissionGroup(EntityReference appRef, InvolvementGroupRecord ig, String pgNameStem) {
        PermissionGroupRecord pg = createGroup(pgNameStem);

        setupPermissionGroupEntry(appRef, pg.getId());
        setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.APPLICATION,
                Operation.ATTEST,
                null);

        return pg;
    }

    public PermissionGroupRecord createGroup(String pgNameStem) {
        String pgName = mkName(pgNameStem, "_pg");
        PermissionGroupRecord pg = dsl.newRecord(PERMISSION_GROUP);
        pg.setDescription("test group: " + pgName);
        pg.setIsDefault(false);
        pg.setExternalId(pgName);
        pg.setName(pgName);
        pg.setProvenance(mkName(pgNameStem, "prov"));
        pg.insert();
        return pg;
    }


    public void setupPermissionGroupInvolvement(Long igId,
                                                Long pgId,
                                                EntityKind subjectKind,
                                                EntityKind parentKind,
                                                Operation operation,
                                                EntityReference qualifierRef) {
        PermissionGroupInvolvementRecord pgi = dsl.newRecord(PERMISSION_GROUP_INVOLVEMENT);
        pgi.setPermissionGroupId(pgId);
        pgi.setInvolvementGroupId(igId);
        pgi.setOperation(operation.name());
        pgi.setSubjectKind(subjectKind.name());
        pgi.setParentKind(parentKind.name());
        if (qualifierRef != null) {
            pgi.setQualifierId(qualifierRef.id());
            pgi.setQualifierKind(qualifierRef.kind().name());
        }
        pgi.insert();
    }


    public void setupPermissionGroupEntry(EntityReference appRef, Long pgId) {
        PermissionGroupEntryRecord pge = dsl.newRecord(PERMISSION_GROUP_ENTRY);
        pge.setPermissionGroupId(pgId);
        pge.setEntityKind(EntityKind.APPLICATION.name());
        pge.setEntityId(appRef.id());
        pge.insert();
    }


    public InvolvementGroupRecord setupInvolvementGroup(Long involvementKindId, String igNameStem) {
        String igName = mkName(igNameStem, "_ig");
        InvolvementGroupRecord ig = dsl.newRecord(INVOLVEMENT_GROUP);
        ig.setName(igName);
        ig.setExternalId(igName);
        ig.setProvenance(mkName(igNameStem, "prov"));
        ig.insert();

        InvolvementGroupEntryRecord ige = dsl.newRecord(INVOLVEMENT_GROUP_ENTRY);
        ige.setInvolvementGroupId(ig.getId());
        ige.setInvolvementKindId(involvementKindId);
        ige.insert();
        return ig;
    }

}
