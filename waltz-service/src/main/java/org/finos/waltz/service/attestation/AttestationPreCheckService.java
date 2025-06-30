package org.finos.waltz.service.attestation;

import org.finos.waltz.model.attestation.ViewpointAttestationPreChecks;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.data.attestation.AttestationPreCheckDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.attestation.LogicalFlowAttestationPreChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class AttestationPreCheckService {

    private final AttestationPreCheckDao attestationPreCheckDao;
    private final SettingsService settingsService;

    @Autowired
    public AttestationPreCheckService(AttestationPreCheckDao attestationPreCheckDao,
                                      SettingsService settingsService) {
        this.attestationPreCheckDao = checkNotNull(attestationPreCheckDao, "AttestationPreCheckEvaluatorDao cannot be null");
        this.settingsService = checkNotNull(settingsService, "settingsService cannot be null");
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

        return failures;
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
