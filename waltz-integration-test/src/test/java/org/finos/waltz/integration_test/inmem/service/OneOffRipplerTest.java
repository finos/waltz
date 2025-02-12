package org.finos.waltz.integration_test.inmem.service;

import liquibase.pro.packaged.A;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentRipplerJobStep;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.ImmutableSaveAssessmentRatingCommand;
import org.finos.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.settings.ImmutableSetting;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.assessment_rating.OneOffRippler;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.LogicalFlowHelper;
import org.finos.waltz.test_common.helpers.PhysicalFlowHelper;
import org.finos.waltz.test_common.helpers.PhysicalSpecHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.Assert.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OneOffRipplerTest extends BaseInMemoryIntegrationTest {

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
    private SettingsService settingsService;

    @Autowired
    private OneOffRippler oneOffRippler;

    @Value.Immutable
    interface OneOffRipplerTestConfig {
        EntityReference fromRef();

        AssessmentDefinition fromAssessmentDefn();

        AssessmentDefinition targetAssessmentDefn();

        Set<Long> ratingItemIds();
    }

    @Test
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
                        oneOffRippler.rippleAssessments(cfg.fromAssessmentDefn(), cfg.fromRef());
                    } catch (InsufficientPrivelegeException e) {
                        e.printStackTrace();
                    }
                });

        long addCounts = assessmentRatingService
                .findByDefinitionId(cfg.targetAssessmentDefn().id().get())
                .stream()
                .count();

        assertEquals((long)cfg.ratingItemIds().size(), addCounts);
    }

    @Test
    public void assessmentRemoveRipples() {
        userHelper.createUserWithSystemRoles("test", SetUtilities.asSet(SystemRole.ADMIN));
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

        AssessmentDefinition fromDef = saveAssessmentDefn(fromDefName, "fromDef", schemeId, EntityKind.PHYSICAL_FLOW, false);
        AssessmentDefinition toDef = saveAssessmentDefn(toDefName, "toDef", schemeId, EntityKind.LOGICAL_DATA_FLOW, true);

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
