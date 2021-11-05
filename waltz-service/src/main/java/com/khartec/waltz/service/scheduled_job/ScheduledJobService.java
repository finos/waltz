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

package com.khartec.waltz.service.scheduled_job;


import com.khartec.waltz.service.attestation.AttestationRunService;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import com.khartec.waltz.service.flow_classification_rule.FlowClassificationRuleService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.finos.waltz.common.ExcludeFromIntegrationTesting;
import org.finos.waltz.data.scheduled_job.ScheduledJobDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.scheduled_job.JobKey;
import org.finos.waltz.model.scheduled_job.JobLifecycleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static org.finos.waltz.common.Checks.checkNotNull;

@ExcludeFromIntegrationTesting
@Service
public class ScheduledJobService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledJobService.class);

    private final DataTypeUsageService dataTypeUsageService;
    private final EntityHierarchyService entityHierarchyService;
    private final FlowClassificationRuleService flowClassificationRuleService;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalSpecDataTypeService physicalSpecDataTypeService;
    private final ScheduledJobDao scheduledJobDao;
    private final AttestationRunService attestationRunService;


    @Autowired
    public ScheduledJobService(DataTypeUsageService dataTypeUsageService,
                               EntityHierarchyService entityHierarchyService,
                               FlowClassificationRuleService flowClassificationRuleService,
                               LogicalFlowService logicalFlowService,
                               PhysicalSpecDataTypeService physicalSpecDataTypeService,
                               ScheduledJobDao scheduledJobDao,
                               AttestationRunService attestationRunService) {
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(flowClassificationRuleService, "flowClassificationRuleService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalSpecDataTypeService, "physicalSpecDataTypeService cannot be null");
        checkNotNull(scheduledJobDao, "scheduledJobDao cannot be null");
        checkNotNull(attestationRunService, "attestationRunService cannot be null");

        this.dataTypeUsageService = dataTypeUsageService;
        this.entityHierarchyService = entityHierarchyService;
        this.flowClassificationRuleService = flowClassificationRuleService;
        this.logicalFlowService = logicalFlowService;
        this.physicalSpecDataTypeService = physicalSpecDataTypeService;
        this.scheduledJobDao = scheduledJobDao;
        this.attestationRunService = attestationRunService;
    }


    @Scheduled(fixedRate = 300_000)
    public void run() {
        Thread.currentThread().setName("WaltzScheduledJobService");
        runIfNeeded(JobKey.HIERARCHY_REBUILD_CHANGE_INITIATIVE,
                (jk) -> entityHierarchyService.buildFor(EntityKind.CHANGE_INITIATIVE));

        runIfNeeded(JobKey.HIERARCHY_REBUILD_DATA_TYPE,
                (jk) -> entityHierarchyService.buildFor(EntityKind.DATA_TYPE));

        runIfNeeded(JobKey.HIERARCHY_REBUILD_ENTITY_STATISTICS,
                (jk) -> entityHierarchyService.buildFor(EntityKind.ENTITY_STATISTIC));

        runIfNeeded(JobKey.HIERARCHY_REBUILD_MEASURABLE,
                (jk) -> entityHierarchyService.buildFor(EntityKind.MEASURABLE));

        runIfNeeded(JobKey.HIERARCHY_REBUILD_ORG_UNIT,
                (jk) -> entityHierarchyService.buildFor(EntityKind.ORG_UNIT));

        runIfNeeded(JobKey.HIERARCHY_REBUILD_PERSON,
                (jk) -> entityHierarchyService.buildFor(EntityKind.PERSON));

        runIfNeeded(JobKey.DATA_TYPE_RIPPLE_PHYSICAL_TO_LOGICAL,
                (jk) -> physicalSpecDataTypeService.rippleDataTypesToLogicalFlows());

        runIfNeeded(JobKey.DATA_TYPE_USAGE_RECALC_APPLICATION,
                (jk) -> dataTypeUsageService.recalculateForAllApplications());

        runIfNeeded(JobKey.AUTH_SOURCE_RECALC_FLOW_RATINGS,
                (jk) -> flowClassificationRuleService.fastRecalculateAllFlowRatings());

        runIfNeeded(JobKey.LOGICAL_FLOW_CLEANUP_ORPHANS,
                (jk) -> logicalFlowService.cleanupOrphans());

        runIfNeeded(JobKey.ATTESTATION_ISSUE_INSTANCES,
                (jk) -> attestationRunService.issueInstancesForPendingRuns());
    }


    private void runIfNeeded(JobKey jobKey, Consumer<JobKey> jobExecutor) {
        try {
            if (scheduledJobDao.isJobRunnable(jobKey)
                    && scheduledJobDao.markJobAsRunning(jobKey)) {
                jobExecutor
                        .andThen((jk) -> scheduledJobDao.updateJobStatus(jk, JobLifecycleStatus.COMPLETED))
                        .accept(jobKey);
            }
        } catch (Exception e) {
            LOG.error("Failed to run job: " + jobKey, e);
            scheduledJobDao.updateJobStatus(jobKey, JobLifecycleStatus.ERRORED);
        }
    }

}
