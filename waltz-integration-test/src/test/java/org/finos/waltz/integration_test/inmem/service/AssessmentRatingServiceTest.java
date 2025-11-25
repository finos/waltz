package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentRipplerJobStep;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.ImmutableRemoveAssessmentRatingCommand;
import org.finos.waltz.model.assessment_rating.ImmutableSaveAssessmentRatingCommand;
import org.finos.waltz.model.assessment_rating.ImmutableUpdateRatingCommand;
import org.finos.waltz.model.assessment_rating.RemoveAssessmentRatingCommand;
import org.finos.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.rating.ImmutableRatingSchemeItem;
import org.finos.waltz.model.settings.ImmutableSetting;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.ChangeLogHelper;
import org.finos.waltz.test_common.helpers.LogicalFlowHelper;
import org.finos.waltz.test_common.helpers.NameHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.helpers.PhysicalFlowHelper;
import org.finos.waltz.test_common.helpers.PhysicalSpecHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AssessmentRatingServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AssessmentDefinitionService assessmentDefinitionService;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private AssessmentRatingService assessmentRatingService;

    @Autowired
    private LogicalFlowHelper logicalFlowHelper;

    @Autowired
    private PhysicalFlowHelper physicalFlowHelper;

    @Autowired
    private PhysicalSpecHelper physicalSpecHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private ChangeLogHelper changeLogHelper;

    @Autowired
    private RatingSchemeService schemeService;

    @Value.Immutable
    interface OneOffRipplerTestConfig {
        EntityReference fromRef();

        AssessmentDefinition fromAssessmentDefn();

        AssessmentDefinition targetAssessmentDefn();

        Set<Long> ratingItemIds();

    }

    @Test
//    @Disabled
    public void assessmentAddRipples() {
        userHelper.createUserWithSystemRoles("test", SetUtilities.asSet(SystemRole.ADMIN));
        OneOffRipplerTestConfig cfg = mkWorld("assessmentAddRipples");

        cfg.ratingItemIds().stream()
                .forEach(t -> {
                    SaveAssessmentRatingCommand cmd = ImmutableSaveAssessmentRatingCommand.builder()
                            .entityReference(cfg.fromRef())
                            .assessmentDefinitionId(cfg.fromAssessmentDefn().id().get())
                            .ratingId(t)
                            .lastUpdatedBy("test")
                            .build();
                    try {
                        assessmentRatingService.store(cmd, "test");
                    } catch (InsufficientPrivelegeException e) {
                        e.printStackTrace();
                    }
                });

        long addCounts = assessmentRatingService
                .findByDefinitionId(cfg.targetAssessmentDefn().id().get())
                .stream()
                .count();

        assertEquals(cfg.ratingItemIds().size(), addCounts);
    }

    @Test
//    @Disabled
    public void assessmentRemoveRipples() {
        userHelper.createUserWithSystemRoles("test", SetUtilities.asSet(SystemRole.ADMIN));
        OneOffRipplerTestConfig cfg = mkWorld("assessmentAddRipples");

        cfg.ratingItemIds().stream()
                .forEach(t -> {
                    SaveAssessmentRatingCommand cmd = ImmutableSaveAssessmentRatingCommand.builder()
                            .entityReference(cfg.fromRef())
                            .assessmentDefinitionId(cfg.fromAssessmentDefn().id().get())
                            .ratingId(t)
                            .lastUpdatedBy("test")
                            .build();
                    try {
                        assessmentRatingService.store(cmd, "test");
                    } catch (InsufficientPrivelegeException e) {
                        e.printStackTrace();
                    }
                });

        long addCounts = assessmentRatingService
                .findByDefinitionId(cfg.targetAssessmentDefn().id().get())
                .stream()
                .count();

        Long singleRating = cfg.ratingItemIds().stream().findFirst().get();

        UserTimestamp lastUpdate = UserTimestamp.mkForUser("test");
        RemoveAssessmentRatingCommand command = ImmutableRemoveAssessmentRatingCommand.builder()
                .entityReference(cfg.fromRef())
                .assessmentDefinitionId(cfg.fromAssessmentDefn().id().get())
                .ratingId(singleRating)
                .lastUpdatedAt(lastUpdate.at())
                .lastUpdatedBy(lastUpdate.by())
                .build();
        try {
            assessmentRatingService.remove(command, "test");
        } catch (InsufficientPrivelegeException e) {
            e.printStackTrace();
        }

        long updatedCounts = assessmentRatingService
                .findByDefinitionId(cfg.targetAssessmentDefn().id().get())
                .stream()
                .count();

        // verify that one record has been removed
        assertEquals(addCounts - 1, updatedCounts);

        cfg.ratingItemIds()
                .stream()
                .filter(t -> t.longValue() != singleRating.longValue())
                .forEach(t -> {
                    RemoveAssessmentRatingCommand command1 = ImmutableRemoveAssessmentRatingCommand.builder()
                            .entityReference(cfg.fromRef())
                            .assessmentDefinitionId(cfg.fromAssessmentDefn().id().get())
                            .ratingId(t)
                            .lastUpdatedAt(lastUpdate.at())
                            .lastUpdatedBy(lastUpdate.by())
                            .build();
                    try {
                        assessmentRatingService.remove(command1, "test");
                    } catch (InsufficientPrivelegeException e) {
                        e.printStackTrace();
                    }
                });

        long finalCounts = assessmentRatingService
                .findByDefinitionId(cfg.targetAssessmentDefn().id().get())
                .stream()
                .count();

        assertEquals(0, finalCounts);
    }

    @Test
//    @Disabled
    public void createUpdateAndRemoveSingleRating() throws InsufficientPrivelegeException {
        String adminUser = NameHelper.mkUserId("adminUser");
        String userWithPerms = NameHelper.mkUserId("userWithPerms");
        String userWithoutPerms = NameHelper.mkUserId("userWithoutPerms");
        String name = NameHelper.mkName("testAssessment");
        String role = NameHelper.mkName("testRole");

        personHelper.createPerson(userWithPerms);
        personHelper.createPerson(userWithoutPerms);

        SchemeDetail schemeDetail = createScheme();

        AssessmentDefinition def = ImmutableAssessmentDefinition.builder()
                .name(name)
                .description("desc")
                .isReadOnly(false)
                .permittedRole(role)
                .entityKind(EntityKind.APPLICATION)
                .lastUpdatedBy(adminUser)
                .visibility(AssessmentVisibility.SECONDARY)
                .ratingSchemeId(schemeDetail.id)
                .build();

        long defId = assessmentDefinitionService.save(def);

        assessmentDefinitionService.save(ImmutableAssessmentDefinition
                .copyOf(def)
                .withId(defId)
                .withDescription("updated desc"));

        Collection<AssessmentDefinition> allDefs = assessmentDefinitionService.findAll();

        AssessmentDefinition found = find(
                d -> d.id().equals(Optional.of(defId)),
                allDefs)
                .orElseThrow(AssertionError::new);

        Assertions.assertEquals(
                "updated desc",
                found.description());

        Assertions.assertEquals(
                found,
                assessmentDefinitionService.getById(defId));

        EntityReference app1 = appHelper.createNewApp(NameHelper.mkName("app1"), ouIds.a);
        EntityReference app2 = appHelper.createNewApp(NameHelper.mkName("app2"), ouIds.b);

        SaveAssessmentRatingCommand cmd = ImmutableSaveAssessmentRatingCommand
                .builder()
                .assessmentDefinitionId(defId)
                .entityReference(app1)
                .ratingId(schemeDetail.y)
                .lastUpdatedBy(userWithPerms)
                .build();

        try {
            assessmentRatingService.store(cmd, userWithoutPerms);
            fail("should have thrown an exception as user cannot update assessment");
        } catch (InsufficientPrivelegeException ipe) {
            // pass
        }

        userHelper.createUserWithRoles(userWithPerms, role);
        assessmentRatingService.store(cmd, userWithPerms);

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(
                app1,
                Operation.ADD);

        Optional<AssessmentRating> ratingCreated = find(
                r -> r.assessmentDefinitionId() == defId && r.ratingId() == schemeDetail.y,
                assessmentRatingService.findForEntity(app1));

        assertTrue(ratingCreated.isPresent());
        assertTrue(assessmentRatingService.findForEntity(app2).isEmpty());

        assessmentRatingService.updateRating(
                ratingCreated.get().id().get(),
                ImmutableUpdateRatingCommand.builder()
                        .newRatingId(schemeDetail.n)
                        .build(),
                userWithPerms);

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(
                app1,
                Operation.UPDATE);

        assertNotNull(find(
                r -> r.assessmentDefinitionId() == defId && r.ratingId() == schemeDetail.n,
                assessmentRatingService.findForEntity(app1)));

        List<AssessmentRating> allRatingsAfterUpdate = assessmentRatingService.findByDefinitionId(defId);
        Assertions.assertEquals(1, allRatingsAfterUpdate.size());
        assertTrue(
                find(
                        r -> r.entityReference().equals(app1) && r.ratingId() == schemeDetail.n,
                        allRatingsAfterUpdate)
                        .isPresent());

        assessmentRatingService.remove(
                ImmutableRemoveAssessmentRatingCommand.builder()
                        .assessmentDefinitionId(defId)
                        .entityReference(app1)
                        .ratingId(schemeDetail.n)
                        .lastUpdatedBy(userWithPerms)
                        .build(),
                userWithPerms);

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(
                app1,
                Operation.REMOVE);

        assertTrue(assessmentRatingService.findForEntity(app1).isEmpty());

        List<AssessmentRating> allRatingsAfterRemoval = assessmentRatingService.findByDefinitionId(defId);
        assertTrue(allRatingsAfterRemoval.isEmpty());
    }


    private static class SchemeDetail {
        long id;
        long y;
        long n;
        long m;
    }


    private SchemeDetail createScheme() {
        long schemeId = ratingSchemeHelper.createEmptyRatingScheme(NameHelper.mkName("testScheme"));
        Long y = schemeService.saveRatingItem(
                schemeId,
                ImmutableRatingSchemeItem.builder()
                        .name("yes")
                        .description("ydesc")
                        .ratingSchemeId(schemeId)
                        .position(10)
                        .color("green")
                        .rating("Y")
                        .build());

        Long n = schemeService.saveRatingItem(
                schemeId,
                ImmutableRatingSchemeItem.builder()
                        .name("no")
                        .description("ndesc")
                        .ratingSchemeId(schemeId)
                        .position(20)
                        .color("red")
                        .rating("N")
                        .build());

        Long m = schemeService.saveRatingItem(
                schemeId,
                ImmutableRatingSchemeItem.builder()
                        .name("maybe")
                        .description("mdesc")
                        .ratingSchemeId(schemeId)
                        .position(30)
                        .color("yellow")
                        .rating("M")
                        .userSelectable(false)
                        .build());

        SchemeDetail detail = new SchemeDetail();
        detail.id = schemeId;
        detail.y = y;
        detail.n = n;
        detail.m = m;
        return detail;
    }

    private OneOffRipplerTestConfig mkWorld(String stem) {
        String app1Name = mkName(stem, "app1");
        String app2Name = mkName(stem, "app2");
        String phySpecName = mkName(stem, "spec");
        String pf1Name = mkName(stem, "pf1");
        String schemeName = mkName(stem, "scheme");
        String fromDefName = mkName(stem, "fromDef");
        String toDefName = mkName(stem, "toDef");

        EntityReference app1Ref = appHelper.createNewApp(app1Name, 1000001L);
        EntityReference app2Ref = appHelper.createNewApp(app2Name, 1000001L);

        Long physicalSpecId = physicalSpecHelper.createPhysicalSpec(app2Ref, phySpecName);
        LogicalFlow logicalFlow = logicalFlowHelper.createLogicalFlow(app1Ref, app2Ref);
        EntityReference pf1 = physicalFlowHelper
                .createPhysicalFlow(logicalFlow.id().get(), physicalSpecId, pf1Name)
                .entityReference();

        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(schemeName);
        Set<Long> ratingItems = saveRatingItems(schemeId, SetUtilities.asSet("R1", "R2", "R3"));

        AssessmentDefinition fromDef = saveAssessmentDefn(fromDefName, fromDefName, schemeId, EntityKind.PHYSICAL_FLOW, false);
        AssessmentDefinition toDef = saveAssessmentDefn(toDefName, toDefName, schemeId, EntityKind.LOGICAL_DATA_FLOW, true);

        AssessmentRipplerJobConfiguration rippleConfig = ImmutableAssessmentRipplerJobConfiguration.builder()
                .name("job.RIPPLE_ASSESSMENTS.test")
                .addSteps(ImmutableAssessmentRipplerJobStep.builder()
                        .fromDef(fromDef.externalId().get())
                        .toDef(toDef.externalId().get())
                        .build())
                .build();

        settingsService.create(ImmutableSetting.builder()
                .name(rippleConfig.name())
                .value(mkSettingValue(rippleConfig))
                .description("Ripple Setting")
                .build());

        return ImmutableOneOffRipplerTestConfig.builder()
                .fromRef(pf1)
                .fromAssessmentDefn(fromDef)
                .targetAssessmentDefn(toDef)
                .ratingItemIds(ratingItems)
                .build();
    }

    private Set<Long> saveRatingItems(Long schemeId, Set<String> ratingNames) {
        return ratingNames.stream()
                .map(t -> ratingSchemeHelper.saveRatingItem(schemeId, t, 0, "#FFFFFF", t))
                .collect(Collectors.toSet());
    }

    private AssessmentDefinition saveAssessmentDefn(String defName, String externalId, Long schemeId, EntityKind entityKind, boolean isReadOnly) {
        AssessmentDefinition def = ImmutableAssessmentDefinition.builder()
                .name(defName)
                .externalId(externalId)
                .description(externalId)
                .cardinality(Cardinality.ZERO_MANY)
                .entityKind(entityKind)
                .isReadOnly(isReadOnly)
                .permittedRole(SystemRole.ADMIN.name())
                .ratingSchemeId(schemeId)
                .visibility(AssessmentVisibility.PRIMARY)
                .lastUpdatedBy("test")
                .build();

        Long id = assessmentDefinitionService.save(def);
        return ImmutableAssessmentDefinition.builder()
                .id(id.longValue())
                .from(def)
                .build();
    }

    private String mkSettingValue(AssessmentRipplerJobConfiguration cfg) {
        return format("[{ \"from\": \"%s\", \"to\": \"%s\"}]",
                cfg.steps().get(0).fromDef(),
                cfg.steps().get(0).toDef());
    }
}
