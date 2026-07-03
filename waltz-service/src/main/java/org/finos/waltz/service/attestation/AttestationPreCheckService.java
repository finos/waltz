package org.finos.waltz.service.attestation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.data.attestation.AttestationPreCheckDao;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao; // Import MeasurableCategoryDao
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.attestation.AttestationPreCheckCommandResponse;
import org.finos.waltz.model.attestation.ImmutableAttestationPreCheckCommandResponse;
import org.finos.waltz.model.attestation.ImmutableAttestationPrimaryFlagSetting;
import org.finos.waltz.model.attestation.LogicalFlowAttestationPreChecks;
import org.finos.waltz.model.attestation.ViewpointAttestationPreChecks;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class AttestationPreCheckService {

    private static final String ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY = "ATTESTATION_PRECHECK_PRIMARY_FLAG";
    private static final String NO_DATAFLOW_DECLARATION_ASSESS_ID = "NO_DATAFLOW_DECLARATION_ASSESS_ID";
    private static final String OPTIONAL_DATAFLOW_GROUP = "OPTIONAL_DATAFLOW_GROUP";

    private final AttestationPreCheckDao attestationPreCheckDao;
    private final SettingsService settingsService;
    private final ProposedFlowWorkflowService proposedFlowWorkflowService;
    private final PhysicalFlowService physicalFlowService;
    private final EntityWorkflowService entityWorkflowService;
    private final MeasurableCategoryDao measurableCategoryDao; // Inject MeasurableCategoryDao
    private final AppGroupService appGroupService;
    private final AssessmentRatingService assessmentRatingService;
    private final RatingSchemeService ratingSchemeService;

    @Autowired
    public AttestationPreCheckService(AttestationPreCheckDao attestationPreCheckDao,
                                      SettingsService settingsService,
                                      ProposedFlowWorkflowService proposedFlowWorkflowService,
                                      PhysicalFlowService physicalFlowService,
                                      EntityWorkflowService entityWorkflowService,
                                      MeasurableCategoryDao measurableCategoryDao, // Add MeasurableCategoryDao to constructor
                                      AppGroupService appGroupService, AssessmentRatingService assessmentRatingService, RatingSchemeService ratingSchemeService) {
        this.attestationPreCheckDao = checkNotNull(attestationPreCheckDao, "AttestationPreCheckEvaluatorDao cannot be null");
        this.settingsService = checkNotNull(settingsService, "settingsService cannot be null");
        this.proposedFlowWorkflowService = proposedFlowWorkflowService;
        this.physicalFlowService = physicalFlowService;
        this.entityWorkflowService = entityWorkflowService;
        this.measurableCategoryDao = checkNotNull(measurableCategoryDao, "measurableCategoryDao cannot be null"); // Initialize MeasurableCategoryDao
        this.appGroupService = checkNotNull(appGroupService, "appGroupService cannot be null");
        this.assessmentRatingService = checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");
        this.ratingSchemeService = checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
    }


    public List<String> calcLogicalFlowPreCheckFailures(EntityReference ref) {

        Map<String, String> messageTemplates = settingsService.indexByPrefix("attestation.logical-flow.fail");
        LogicalFlowAttestationPreChecks preChecks = attestationPreCheckDao.calcLogicalFlowAttestationPreChecks(ref);

        List<String> failures = new ArrayList<>();

        if (preChecks.flowCount() == 0 && !preChecks.exemptFromFlowCountCheck()) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.logical-flow.fail.count",
                    "Cannot attest as there are no recorded relevant flows",
                    preChecks.flowCount()));
        }

        if (preChecks.deprecatedCount() > 0 && !preChecks.exemptFromDeprecatedCheck()) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.logical-flow.fail.deprecated",
                    "Cannot attest as there are deprecated data type usages (%d violation/s)",
                    preChecks.deprecatedCount()));
        }

        if (preChecks.unknownCount() > 0 && !preChecks.exemptFromUnknownCheck()) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.logical-flow.fail.unknown",
                    "Cannot attest as there are unknown data type usages (%d violation/s)",
                    preChecks.unknownCount()));
        }

        return failures;
    }

    public List<String> calcViewpointPreCheckFailures(EntityReference entityReference, Long attestedEntityId) {

        Map<String, String> messageTemplates = settingsService.indexByPrefix("attestation.viewpoints.fail");
        ViewpointAttestationPreChecks preChecks = attestationPreCheckDao
                .calcViewpointAttestationPreChecks(entityReference, attestedEntityId);

        List<String> failures = new ArrayList<>();

        if (preChecks.mappingCount() == 0) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.viewpoint.fail.count",
                    "Cannot attest because at least one viewpoint mapping should be present.",
                    preChecks.mappingCount()));
        }

        if (preChecks.nonConcreteCount() > 0) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.viewpoint.fail.nonConcrete.count",
                    "Cannot attest as %d abstract mapping(s) exists. Abstract nodes cannot be used in mappings as they are not specific enough.",
                    preChecks.nonConcreteCount()));
        }

        if (preChecks.mappingCount() > 0 && preChecks.totalAllocation() != 100) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.viewpoint.fail.total.allocation",
                    "Cannot attest as the total allocation for viewpoint mappings is not equal to 100",
                    0));
        }

        if (preChecks.mappingCount() > 0 && preChecks.zeroAllocationCount() > 0) {
            failures.add(mkFailureMessage(
                    messageTemplates,
                    "attestation.viewpoint.fail.zeroAllocation.count",
                    "Cannot attest as %d viewpoint mapping(s) exists with no allocation",
                    preChecks.zeroAllocationCount()));
        }

        // Check if the measurable category allows primary ratings using MeasurableCategoryDao
        boolean allowPrimaryRatings = measurableCategoryDao.getById(attestedEntityId).allowPrimaryRatings();

        if (allowPrimaryRatings) { // Wrap the primary flag check in this condition
            Optional<String> primaryFlagSetting = settingsService.getValue(ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY);

            ObjectMapper mapper = new ObjectMapper();
            List<ImmutableAttestationPrimaryFlagSetting> primaryFlagSettings = primaryFlagSetting
                    .map(str -> {
                        try {
                            ImmutableAttestationPrimaryFlagSetting[] settings = mapper.readValue(str, ImmutableAttestationPrimaryFlagSetting[].class);
                            return Arrays.asList(settings);
                        } catch (JsonProcessingException e) {
                            // LOG.error("Failed to parse AttestationPrimaryFlagSetting(s) from settings value: {}", str); // Need to add LOG
                            return new ArrayList<ImmutableAttestationPrimaryFlagSetting>();
                        }
                    })
                    .orElseGet(ArrayList::new);

            boolean isPrimaryMandatory = primaryFlagSettings.stream()
                    .filter(s -> Objects.equals(s.measurableCategoryId(), attestedEntityId))
                    .map(ImmutableAttestationPrimaryFlagSetting::isPrimaryMandatory)
                    .findFirst()
                    .orElse(false);
            if (isPrimaryMandatory && preChecks.primaryRatingsCount() == 0) {
                failures.add(mkFailureMessage(
                        messageTemplates,
                        "attestation.viewpoint.fail.primaryFlag.count",
                        "Cannot attest as one of the viewpoint mapping should have a Primary flag",
                        preChecks.mappingCount()));
            }
        }
        return failures;
    }

    public AttestationPreCheckCommandResponse calcLogicalFlowPreCheckFailuresWithProposed(EntityReference entityRef, String username) {
        Map<String, String> messageTemplates = settingsService.indexByPrefix("attestation.logical-flow.fail");

        EntityWorkflowDefinition workflowDefinition = entityWorkflowService.searchByName(ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        // Rule 1: Block if user is a target approver for upstream pending flow for this app
        if (proposedFlowWorkflowService.isAppInvolvedInPendingApprovals(entityRef, username, workflowDefinition.id().get())) {

            return ImmutableAttestationPreCheckCommandResponse.builder()
                    .outcome(CommandOutcome.FAILURE)
                    .message(mkFailureMessage(
                            messageTemplates,
                            "attestation.logical-flow.fail.pending.approval",
                            "Cannot attest as there are pending flows requiring your app's approval. Please review them first.",
                            0))
                    .build();
        }

        // 1. Get the base pre-check results
        LogicalFlowAttestationPreChecks preChecks = attestationPreCheckDao.calcLogicalFlowAttestationPreChecks(entityRef);

        boolean hasPendingProposals = false;
        // Rule 2: If there are no flows, block unless a creation is pending
        if (preChecks.flowCount() == 0 && !preChecks.exemptFromFlowCountCheck()) {
            // Priority Check: Does the app have pending flow creations?
            if (proposedFlowWorkflowService.hasPendingCreations(entityRef, workflowDefinition.id().get())) {
                hasPendingProposals = true;
            } else {
                // No pending flow creations. Check if the app belongs to the Optional Dataflow Group and has proper reasoning.
                Optional<String> assessIdSetting = settingsService.getValue(NO_DATAFLOW_DECLARATION_ASSESS_ID);
                Optional<String> appGroupSetting = settingsService.getValue(OPTIONAL_DATAFLOW_GROUP);

                if (assessIdSetting.isPresent() && appGroupSetting.isPresent()) {
                    long assessDefId = Long.parseLong(assessIdSetting.get());
                    long appGroupId = Long.parseLong(appGroupSetting.get());

                    // Step 3: Check if app belongs to the application group
                    boolean isInGroup = appGroupService.isAppInGroup(appGroupId, entityRef.id());
                    if (isInGroup) {
                        // Step 4: Check if assessed with the assessment_definition id
                        Optional<AssessmentRating> ratingOpt = assessmentRatingService.getRatingForEntityAndDefinition(entityRef, assessDefId);
                        if (ratingOpt.isPresent()) {
                            // Step 5: Assessed -> allow attestation with success message
                            String ratingValue = ratingSchemeService.getRatingSchemeItemById(ratingOpt.get().ratingId()).name();
                            return ImmutableAttestationPreCheckCommandResponse.builder()
                                    .outcome(CommandOutcome.SUCCESS)
                                    .message(format("You are attesting that your application has no data flows for the reason %s", ratingValue))
                                    .build();
                        } else {
                            // Step 7: In group but no reasoning (rating) given
                            return ImmutableAttestationPreCheckCommandResponse.builder()
                                    .outcome(CommandOutcome.FAILURE)
                                    .message("Cannot attest as no dataflows and no reasoning given")
                                    .build();
                        }
                    }
                }

                // Fallback (Step 6 / Settings Missing): App has no pending creations, is not in the group, or settings were missing entirely
                return ImmutableAttestationPreCheckCommandResponse.builder()
                        .outcome(CommandOutcome.FAILURE)
                        .message(mkFailureMessage(
                                messageTemplates,
                                "attestation.logical-flow.fail.count",
                                "Cannot attest as there are no recorded relevant flows",
                                preChecks.flowCount()))
                        .build();
            }
        }

        // Rule 3: If there are unknown/deprecated upstream flows, block unless a removal is pending
        if ((preChecks.deprecatedCount() > 0 && !preChecks.exemptFromDeprecatedCheck()) ||
                (preChecks.unknownCount() > 0 && !preChecks.exemptFromUnknownCheck())) {
            Set<Long> deprecatedOrUnknownFlowIds = attestationPreCheckDao.findDeprecatedOrUnknownFlowIdsForEntity(entityRef);
            Set<Long> deprecatedOrUnknownPhysicalFlowIds = physicalFlowService.findPhysicalFlowIdsWithProblematicDataTypes(deprecatedOrUnknownFlowIds);
            Set<Long> proposedPhysicalFlowIds = proposedFlowWorkflowService.findPhysicalFlowIdsInPendingProposals(deprecatedOrUnknownFlowIds, workflowDefinition.id().get());
            if (deprecatedOrUnknownPhysicalFlowIds.isEmpty() || !Objects.equals(deprecatedOrUnknownPhysicalFlowIds, proposedPhysicalFlowIds)) {

                return ImmutableAttestationPreCheckCommandResponse.builder()
                        .outcome(CommandOutcome.FAILURE)
                        .message(mkFailureMessage(
                                messageTemplates,
                                "attestation.logical-flow.fail.deprecated",
                                "Cannot attest as there are deprecated/unknown data type usages (%d violation/s)",
                                preChecks.deprecatedCount() + preChecks.unknownCount()))
                        .build();
            } else {
                hasPendingProposals = true;
            }
        }

        if (hasPendingProposals) {
            return ImmutableAttestationPreCheckCommandResponse.builder()
                    .outcome(CommandOutcome.SUCCESS)
                    .message(mkFailureMessage(
                            messageTemplates,
                            "attestation.logical-flow.pending.proposed-flow.message",
                            "Given you have pending flow(s), you are still able to attest",
                            0))
                    .build();
        }

        return ImmutableAttestationPreCheckCommandResponse.builder()
                .outcome(CommandOutcome.SUCCESS)
                .build();
    }

    private String mkFailureMessage(Map<String, String> messageTemplates,
                                    String messageKey,
                                    String defaultMessage,
                                    int count) {
        String messageTemplate = messageTemplates.getOrDefault(
                messageKey,
                defaultMessage);

        return format(messageTemplate, count);
    }
}
