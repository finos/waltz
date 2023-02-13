package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.service.permission.permission_checker.LegalEntityRelationshipPermissionChecker;
import org.finos.waltz.test_common.helpers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

@Service
public class LegalEntityRelationshipPermissionCheckerTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private LegalEntityRelationshipPermissionChecker legalEntityRelationshipPermissionChecker;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private UserHelper userHelper;

    private final String stem = "lerpc";


    @Test
    public void findPermissionsForFlow() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
//
//        assertThrows(
//                IllegalArgumentException.class,
//                () -> legalEntityRelationshipPermissionChecker.findPermissionsForFlow(null, u1),
//                "flow id cannot be null");
//
//        assertThrows(
//                IllegalArgumentException.class,
//                () -> legalEntityRelationshipPermissionChecker.findPermissionsForFlow(1L, null),
//                "username cannot be null");
//
//        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
//        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);
//        EntityReference appC = appHelper.createNewApp(mkName(stem, "appC"), ouIds.a1);
//
//        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
//
//        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);
//        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
//
//        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());
//        permissionHelper.setupPermissionGroupEntry(appB, pg.getId());
//        permissionHelper.setupPermissionGroupEntry(appC, pg.getId());
//
//        involvementHelper.createInvolvement(u1Id, privKind, appA);
//
    }

}