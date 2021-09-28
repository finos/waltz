/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.attestation;

import com.khartec.waltz.common.exception.UpdateFailedException;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.attestation.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.external_identifier.ExternalIdValue;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.permission.PermissionGroupService;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.CollectionUtilities.notEmpty;
import static com.khartec.waltz.common.DateTimeUtilities.*;
import static com.khartec.waltz.schema.Tables.APPLICATION;
import static com.khartec.waltz.schema.Tables.ATTESTATION_INSTANCE;


@Service
public class AttestationInstanceService {

    private static final Logger LOG = LoggerFactory.getLogger(AttestationInstanceService.class);

    private final AttestationInstanceDao attestationInstanceDao;
    private final AttestationRunService attestationRunService;
    private final ApplicationService applicationService;
    private final PersonDao personDao;
    private final ChangeLogService changeLogService;
    private final PermissionGroupService permissionGroupService;

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    public AttestationInstanceService(AttestationInstanceDao attestationInstanceDao,
                                      AttestationRunService attestationRunService,
                                      ApplicationService applicationService,
                                      PersonDao personDao, ChangeLogService changeLogService,
                                      PermissionGroupService permissionGroupService) {
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(attestationRunService, "attestationRunService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.attestationRunService = attestationRunService;
        this.applicationService = applicationService;
        this.personDao = personDao;
        this.changeLogService = changeLogService;
        this.permissionGroupService = permissionGroupService;
    }


    public List<AttestationInstance> findByRecipient(String userId, boolean unattestedOnly) {
        checkNotNull(userId, "userId cannot be null");

        return attestationInstanceDao.findByRecipient(userId, unattestedOnly);
    }


    public List<AttestationInstance> findHistoricalForPendingByUserId(String userId) {
        checkNotNull(userId, "userId cannot be null");

        return attestationInstanceDao.findHistoricalForPendingByUserId(userId);
    }


    public List<AttestationInstance> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return attestationInstanceDao.findByEntityReference(ref);
    }


    public boolean attestInstance(long instanceId, String attestedBy) {
        checkNotEmpty(attestedBy, "attestedBy must be provided");

        boolean success = attestationInstanceDao.attestInstance(instanceId, attestedBy, nowUtc());
        if (success) {
            AttestationInstance instance = attestationInstanceDao.getById(instanceId);
            AttestationRun run = attestationRunService.getById(instance.attestationRunId());
            logChange(attestedBy, instance, run.attestedEntityKind());
        }
        return success;
    }


    public List<AttestationInstance> findByRunId(long runId) {
        return attestationInstanceDao.findByRunId(runId);
    }


    public List<Person> findPersonsByInstanceId(long id) {
        return personDao.findPersonsByAttestationInstanceId(id);
    }


    public List<AttestationInstance> findByIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = mkIdSelector(options);
        return attestationInstanceDao.findByIdSelector(selector);
    }


    public int cleanupOrphans() {
        return attestationInstanceDao.cleanupOrphans();
    }


    private Select<Record1<Long>> mkIdSelector(IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(EntityKind.ATTESTATION, selectionOptions);
        return genericSelector.selector();
    }

    private void logChange(String username, AttestationInstance instance, EntityKind attestedKind) {

        String logMessage = EntityKind.APPLICATION.equals(instance.parentEntity().kind())
                ? String.format("Attestation of %s for application %s",
                attestedKind,
                ExternalIdValue.orElse(
                        applicationService
                                .getById(instance.parentEntity().id())
                                .assetCode(),
                        "UNKNOWN"))
                : String.format("Attestation of %s ", attestedKind);

        changeLogService.write(ImmutableChangeLog.builder()
                .message(logMessage)
                .parentReference(instance.parentEntity())
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(attestedKind)
                .operation(Operation.ATTEST)
                .build());
    }


    public boolean attestForEntity(String username, AttestEntityCommand createCommand) {
        checkAttestationPermission(username, createCommand);

        List<AttestationInstance> instancesForEntityForUser = attestationInstanceDao
                .findForEntityByRecipient(
                        createCommand,
                        username,
                        true);

        if (notEmpty(instancesForEntityForUser)) {
            instancesForEntityForUser
                    .forEach(attestation -> {
                        try {
                            Long instanceId = getInstanceId(attestation);
                            attestInstance(instanceId, username);
                        } catch (Exception e) {
                            LOG.error("Failed to attest instance", e);
                        }
                    });

            return true;
        } else {
            IdCommandResponse idCommandResponse = attestationRunService.createRunForEntity(username, createCommand);
            Long runId = getRunId(idCommandResponse);

            Long instanceId = getInstanceId(first(findByRunId(runId)));
            return attestInstance(instanceId, username);
        }
    }


    public Set<LatestMeasurableAttestationInfo> findLatestMeasurableAttestations(EntityReference ref) {
        return attestationInstanceDao.findLatestMeasurableAttestations(ref);
    }


    private void checkAttestationPermission(String username, AttestEntityCommand createCommand) {
        boolean hasAttestationPermission = permissionGroupService.hasPermission(
                createCommand.entityReference(),
                createCommand.attestedEntityKind(),
                username);

        if (!hasAttestationPermission) {
            throw new UpdateFailedException("ATTESTATION_FAILED",
                    "user does not have permission to attest " + createCommand.attestedEntityKind().prettyName());
        }
    }

    private Long getRunId(IdCommandResponse idCommandResponse) throws IllegalStateException {
        return idCommandResponse.id()
                .orElseThrow(() -> new IllegalStateException("Unable to get identifier for this run"));
    }

    private Long getInstanceId(AttestationInstance attestation) throws IllegalStateException {
        return attestation
                .id()
                .orElseThrow(() -> new IllegalStateException("Unable to get identifier for this instance"));
    }


    public Set<ApplicationAttestationInstanceSummary> findApplicationAttestationInstancesForKindAndSelector(
            EntityKind attestedKind,
            Long attestedId,
            ApplicationAttestationInstanceInfo attestationInfo) {

        ApplicationAttestationSummaryFilters filters = attestationInfo.filters();

        Condition condition = getApplicationAttestationFilterCondition(filters);

        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(attestationInfo.selectionOptions());

        return attestationInstanceDao.findApplicationAttestationInstancesForKindAndSelector(
                attestedKind,
                attestedId,
                appIds,
                condition);
    }


    public Set<ApplicationAttestationSummaryCounts> findAttestationInstanceSummaryForSelector(ApplicationAttestationInstanceInfo applicationAttestationInstanceInfo) {

        Condition condition = getApplicationAttestationFilterCondition(applicationAttestationInstanceInfo.filters());

        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(applicationAttestationInstanceInfo.selectionOptions());

        return attestationInstanceDao.findAttestationInstanceSummaryForSelector(appIds, condition);
    }


    private Condition getApplicationAttestationFilterCondition(ApplicationAttestationSummaryFilters filters) {
        Condition criticalityCondition = filters.appCriticality()
                .map(criticality -> APPLICATION.BUSINESS_CRITICALITY.eq(criticality.name()))
                .orElse(DSL.trueCondition());

        Condition lifecyclePhaseCondition = filters.appLifecyclePhase()
                .map(lifecyclePhase -> APPLICATION.LIFECYCLE_PHASE.eq(lifecyclePhase.name()))
                .orElse(DSL.trueCondition());

        Condition dateCondition = filters.attestationsFromDate()
                .map(fromDate -> ATTESTATION_INSTANCE.ATTESTED_AT.ge(Timestamp.valueOf(toLocalDateTime(toSqlDate(fromDate)))))
                .orElse(DSL.trueCondition());

        return dateCondition.and(lifecyclePhaseCondition.and(criticalityCondition));
    }
}
