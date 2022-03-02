package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.AppHelper;
import org.finos.waltz.integration_test.inmem.helpers.InvolvementHelper;
import org.finos.waltz.integration_test.inmem.helpers.PersonHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.permission_group.CheckPermissionCommand;
import org.finos.waltz.model.permission_group.ImmutableCheckPermissionCommand;
import org.finos.waltz.schema.tables.records.*;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.schema.tables.InvolvementGroup.INVOLVEMENT_GROUP;
import static org.finos.waltz.schema.tables.InvolvementGroupEntry.INVOLVEMENT_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Service
public class PermissionGroupServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private PermissionGroupService permissionGroupService;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    private final String stem = "pgst";

    @Test
    public void checkPerms() {
        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        String u2 = mkName(stem, "user2");
        Long u2Id = personHelper.createPerson(u2);
        String u3 = mkName(stem, "user3");
        Long u3Id = personHelper.createPerson(u3);

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.a);

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        long nonPrivKind = involvementHelper.mkInvolvementKind(mkName(stem, "non_privileged"));

        assertTrue(permissionGroupService.hasPermission(mkCommand(u1, appA)), "u1 should have access as is open by default");

        setupSpecificPermissionGroupForApp(appB, privKind);

        involvementHelper.createInvolvement(u1Id, privKind, appB);
        involvementHelper.createInvolvement(u1Id, nonPrivKind, appB);
        involvementHelper.createInvolvement(u2Id, nonPrivKind, appB);

        assertTrue(permissionGroupService.hasPermission(mkCommand(u1, appB)), "u1 should have access as they have the right priv");
        assertFalse(permissionGroupService.hasPermission(mkCommand(u2, appB)), "u2 should not have access as they have the wrong priv");
        assertFalse(permissionGroupService.hasPermission(mkCommand(u3, appB)), "u3 should not have access as they have the no privs");
    }

    private CheckPermissionCommand mkCommand(String u1, EntityReference appA) {
        return ImmutableCheckPermissionCommand
                .builder()
                .parentEntityRef(appA)
                .subjectKind(EntityKind.ATTESTATION)
                .qualifierKind(EntityKind.LOGICAL_DATA_FLOW)
                .user(u1)
                .build();
    }


    private Long setupSpecificPermissionGroupForApp(EntityReference appRef, Long involvementKindId) {
        InvolvementGroupRecord ig = setupInvolvementGroup(involvementKindId);
        PermissionGroupRecord pg = setupPermissionGroup(appRef, ig);
        return pg.getId();
    }


    private PermissionGroupRecord setupPermissionGroup(EntityReference appRef, InvolvementGroupRecord ig) {
        String pgName = mkName(stem, "_pg");
        PermissionGroupRecord pg = dsl.newRecord(PERMISSION_GROUP);
        pg.setDescription("test group: " + pgName);
        pg.setIsDefault(false);
        pg.setExternalId(pgName);
        pg.setName(pgName);
        pg.setProvenance(mkName(stem, "prov"));
        pg.insert();

        PermissionGroupEntryRecord pge = dsl.newRecord(PERMISSION_GROUP_ENTRY);
        pge.setPermissionGroupId(pg.getId());
        pge.setApplicationId(appRef.id());
        pge.insert();

        PermissionGroupInvolvementRecord pgi = dsl.newRecord(PERMISSION_GROUP_INVOLVEMENT);
        pgi.setPermissionGroupId(pg.getId());
        pgi.setSubjectKind(EntityKind.ATTESTATION.name());
        pgi.setQualifierKind(EntityKind.LOGICAL_DATA_FLOW.name());
        pgi.setInvolvementGroupId(ig.getId());
        pgi.insert();
        return pg;
    }

    private InvolvementGroupRecord setupInvolvementGroup(Long involvementKindId) {
        String igName = mkName(stem, "_ig");
        InvolvementGroupRecord ig = dsl.newRecord(INVOLVEMENT_GROUP);
        ig.setName(igName);
        ig.setExternalId(igName);
        ig.setProvenance(mkName(stem, "prov"));
        ig.insert();

        InvolvementGroupEntryRecord ige = dsl.newRecord(INVOLVEMENT_GROUP_ENTRY);
        ige.setInvolvementGroupId(ig.getId());
        ige.setInvolvementKindId(involvementKindId);
        ige.insert();
        return ig;
    }
}
