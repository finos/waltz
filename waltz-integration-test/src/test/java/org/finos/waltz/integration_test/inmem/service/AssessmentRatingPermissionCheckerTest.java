package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.permission.permission_checker.AssessmentRatingPermissionChecker;
import org.finos.waltz.test_common.helpers.*;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Service
public class AssessmentRatingPermissionCheckerTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private AssessmentRatingPermissionChecker assessmentRatingPermissionChecker;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private AssessmentHelper assessmentHelper;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private UserHelper userHelper;

    private final String stem = "arpc";


    @Test
    public void findAssessmentRatingPermissions() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");
        String differentRoleName = mkName(stem, "differentRoleName");

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "assessment permission checker"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", 'R');
        ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", 'A');
        ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", 'G');
        long defnId = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), adminRoleName, AssessmentVisibility.PRIMARY, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentRatingPermissionChecker.findRatingPermissions(null, defnId, u1),
                "entity reference cannot be null");

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId, null),
                "username cannot be null");


        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        Set<Operation> noPermissionsConfigured = assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId, u1);
        assertEquals(emptySet(), noPermissionsConfigured, "If no permission group involvement returns no permissions");

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.ASSESSMENT_RATING,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.ASSESSMENT_DEFINITION, defnId));

        Set<Operation> hasOneOfEditablePermissions = assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId, u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), hasOneOfEditablePermissions, "If has given permission then returns operations for which have the required involvement");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.ASSESSMENT_RATING,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                mkRef(EntityKind.ASSESSMENT_DEFINITION, defnId));

        Set<Operation> noPermsWhereNoInvKind = assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId, u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), noPermsWhereNoInvKind, "Doesn't return perms for operations where user lacks the required inv kind");

        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);

        Set<Operation> hasNoInvolvementWithApplication = assessmentRatingPermissionChecker.findRatingPermissions(appB, defnId, u1);
        assertEquals(
                emptySet(),
                hasNoInvolvementWithApplication,
                "User only has permissions on applications they have an involvement with");

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> hasOverrideRoleForCategory = assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                hasOverrideRoleForCategory,
                "Returns all edit perms where user has the override role on the category");

        Set<Operation> overRideRoleGivesAllEditPermsOnAnyApp = assessmentRatingPermissionChecker.findRatingPermissions(appB, defnId, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp,
                "Override role provides edit permissions on all applications");

        long defnId2 = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), null, AssessmentVisibility.PRIMARY, null);

        Set<Operation> definitionsWithNullPermittedRoleShouldBeEditableByAnyone = assessmentRatingPermissionChecker.findRatingPermissions(appB, defnId2, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                definitionsWithNullPermittedRoleShouldBeEditableByAnyone,
                "Assessments with a null permitted role should be editable by everyone");

        long defnId3 = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), differentRoleName, AssessmentVisibility.PRIMARY, null);

        Set<Operation> permissionsAdhereToQualifier = assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId3, u1);
        assertEquals(
                emptySet(),
                permissionsAdhereToQualifier,
                "Permissions should adhere to the qualifier, having permissions on one definition does not necessarily provide permission on another");

        long defnId4 = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), adminRoleName, AssessmentVisibility.PRIMARY, null);

        Set<Operation> overrideProvidesAccessEvenWhereNoEntryInPermissionsTable = assessmentRatingPermissionChecker.findRatingPermissions(appB, defnId4, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overrideProvidesAccessEvenWhereNoEntryInPermissionsTable,
                "Override should provide edit right even where no entry in permissions table");
    }


    @Test
    public void findAssessmentRatingPermissionsNullGroupId() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "assessment permission checker"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", 'R');
        ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", 'A');
        ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", 'G');
        long defnId = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), adminRoleName, AssessmentVisibility.PRIMARY, null);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        permissionHelper.setupPermissionGroupInvolvement(
                null,
                pg.getId(),
                EntityKind.ASSESSMENT_RATING,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.ASSESSMENT_DEFINITION, defnId));

        Set<Operation> nullInvolvementGroupGivesAllPermissionsForOperation = assessmentRatingPermissionChecker.findRatingPermissions(appA, defnId, u1);

        assertEquals(
                SetUtilities.asSet(Operation.ADD),
                nullInvolvementGroupGivesAllPermissionsForOperation,
                "Null involvement group id gives everyone permissions without needing override but only for described operations");
    }

}
