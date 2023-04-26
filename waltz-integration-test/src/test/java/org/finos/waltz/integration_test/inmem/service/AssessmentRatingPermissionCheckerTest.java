package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_rating.AssessmentDefinitionRatingOperations;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.permission.permission_checker.AssessmentRatingPermissionChecker;
import org.finos.waltz.test_common.helpers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

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
        ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", "R");
        ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", "A");
        ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", "G");
        long defnId = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), adminRoleName, AssessmentVisibility.PRIMARY, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentRatingPermissionChecker.getRatingPermissions(null, defnId, u1),
                "entity reference cannot be null");

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, null),
                "username cannot be null");


        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        AssessmentDefinitionRatingOperations noPermissionsConfigured = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);
        Set<Operation> operationsAcrossAllRatings = noPermissionsConfigured.ratingOperations().stream().flatMap(d -> d.operations().stream()).collect(Collectors.toSet());

        assertEquals(emptySet(), operationsAcrossAllRatings, "If no permission group involvement returns no permissions");

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.ASSESSMENT_RATING,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.ASSESSMENT_DEFINITION, defnId));

        AssessmentDefinitionRatingOperations hasOneOfEditablePermissions = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), hasOneOfEditablePermissions.findDefault(), "If has given permission then returns operations for which have the required involvement");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.ASSESSMENT_RATING,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                mkRef(EntityKind.ASSESSMENT_DEFINITION, defnId));

        AssessmentDefinitionRatingOperations noPermsWhereNoInvKind = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), noPermsWhereNoInvKind.findDefault(), "Doesn't return perms for operations where user lacks the required inv kind");

        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);

        AssessmentDefinitionRatingOperations hasNoInvolvementWithApplication = assessmentRatingPermissionChecker.getRatingPermissions(appB, defnId, u1);
        assertEquals(
                emptySet(),
                hasNoInvolvementWithApplication.findDefault(),
                "User only has permissions on applications they have an involvement with");

        userHelper.createUserWithRoles(u1, adminRoleName);

        AssessmentDefinitionRatingOperations hasOverrideRoleForCategory = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                hasOverrideRoleForCategory.findDefault(),
                "Returns all edit perms where user has the override role on the category");

        AssessmentDefinitionRatingOperations overRideRoleGivesAllEditPermsOnAnyApp = assessmentRatingPermissionChecker.getRatingPermissions(appB, defnId, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp.findDefault(),
                "Override role provides edit permissions on all applications");

        long defnId2 = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), null, AssessmentVisibility.PRIMARY, null);

        AssessmentDefinitionRatingOperations definitionsWithNullPermittedRoleShouldBeEditableByAnyone = assessmentRatingPermissionChecker.getRatingPermissions(appB, defnId2, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                definitionsWithNullPermittedRoleShouldBeEditableByAnyone.findDefault(),
                "Assessments with a null permitted role should be editable by everyone");

        long defnId3 = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), differentRoleName, AssessmentVisibility.PRIMARY, null);

        AssessmentDefinitionRatingOperations permissionsAdhereToQualifier = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId3, u1);
        assertEquals(
                emptySet(),
                permissionsAdhereToQualifier.findDefault(),
                "Permissions should adhere to the qualifier, having permissions on one definition does not necessarily provide permission on another");

        long defnId4 = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), adminRoleName, AssessmentVisibility.PRIMARY, null);

        AssessmentDefinitionRatingOperations overrideProvidesAccessEvenWhereNoEntryInPermissionsTable = assessmentRatingPermissionChecker.getRatingPermissions(appB, defnId4, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overrideProvidesAccessEvenWhereNoEntryInPermissionsTable.findDefault(),
                "Override should provide edit right even where no entry in permissions table");
    }


    @Test
    public void findAssessmentRatingPermissionsNullGroupId() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "assessment permission checker"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", "R");
        ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", "A");
        ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", "G");
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

        AssessmentDefinitionRatingOperations nullInvolvementGroupGivesAllPermissionsForOperation = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);

        assertEquals(
                SetUtilities.asSet(Operation.ADD),
                nullInvolvementGroupGivesAllPermissionsForOperation.findDefault(),
                "Null involvement group id gives everyone permissions without needing override but only for described operations");
    }

    @Test
    public void findAssessmentRatingPermissionsNullPermittedRole() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "assessment permission checker"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", "R");
        ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", "A");
        ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", "G");
        long defnId = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), null, AssessmentVisibility.PRIMARY, null);

        AssessmentDefinitionRatingOperations nullPermittedRoleGivesAllPermissions = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);

        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                nullPermittedRoleGivesAllPermissions.findDefault(),
                "Null permitted role gives everyone permissions without needing override or required involvement");
    }


    @Test
    public void findAssessmentRatingPermissionsReadOnlyDefinition() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "assessment permission checker"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", "R");
        ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", "A");
        ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", "G");
        long defnId = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), null, AssessmentVisibility.PRIMARY, null);
        assessmentHelper.updateDefinitionReadOnly(defnId);

        AssessmentDefinitionRatingOperations readOnlyDefinition = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);

        assertEquals(
                emptySet(),
                readOnlyDefinition.findDefault(),
                "Read only definition should not allow ratings to be edited");
    }


    @Test
    public void findAssessmentRatingPermissionsReadOnlyRating() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "assessment permission checker"));
        Long r1 = ratingSchemeHelper.saveRatingItem(schemeId, "Disinvest", 1, "red", "R");
        Long r2 = ratingSchemeHelper.saveRatingItem(schemeId, "Maintain", 2, "amber", "A");
        Long r3 = ratingSchemeHelper.saveRatingItem(schemeId, "Invest", 3, "green", "G");
        long defnId = assessmentHelper.createDefinition(schemeId, mkName(stem, "assessment permission checker"), null, AssessmentVisibility.PRIMARY, null);
        assessmentHelper.createAssessment(defnId, appA, r1);
        assessmentHelper.updateRatingReadOnly(appA, defnId);

        AssessmentDefinitionRatingOperations readOnlyRating = assessmentRatingPermissionChecker.getRatingPermissions(appA, defnId, u1);

        assertEquals(
                emptySet(),
                readOnlyRating.findForRatingId(r1),
                "Read only rating should not be editable");
    }

}
