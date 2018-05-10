/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.scheduled_job;


import com.khartec.waltz.data.scheduled_job.ScheduledJobDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.scheduled_job.JobKey;
import com.khartec.waltz.model.scheduled_job.JobLifecycleStatus;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ScheduledJobService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledJobService.class);

    private final AuthoritativeSourceService authoritativeSourceService;
    private final ComplexityRatingService complexityRatingService;
    private final DataTypeUsageService dataTypeUsageService;
    private final EntityHierarchyService entityHierarchyService;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalSpecDataTypeService physicalSpecDataTypeService;
    private final ScheduledJobDao scheduledJobDao;


    @Autowired
    public ScheduledJobService(AuthoritativeSourceService authoritativeSourceService,
                               ComplexityRatingService complexityRatingService,
                               DataTypeUsageService dataTypeUsageService,
                               EntityHierarchyService entityHierarchyService,
                               LogicalFlowService logicalFlowService,
                               PhysicalSpecDataTypeService physicalSpecDataTypeService,
                               ScheduledJobDao scheduledJobDao) {
        checkNotNull(authoritativeSourceService, "authoritativeSourceService cannot be null");
        checkNotNull(complexityRatingService, "complexityRatingService cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalSpecDataTypeService, "physicalSpecDataTypeService cannot be null");
        checkNotNull(scheduledJobDao, "scheduledJobDao cannot be null");

        this.authoritativeSourceService = authoritativeSourceService;
        this.complexityRatingService = complexityRatingService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.entityHierarchyService = entityHierarchyService;
        this.logicalFlowService = logicalFlowService;
        this.physicalSpecDataTypeService = physicalSpecDataTypeService;
        this.scheduledJobDao = scheduledJobDao;
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

        runIfNeeded(JobKey.COMPLEXITY_REBUILD,
                (jk) -> complexityRatingService.rebuild());

        runIfNeeded(JobKey.AUTH_SOURCE_RECALC_FLOW_RATINGS,
                (jk) -> authoritativeSourceService.recalculateAllFlowRatings());

        runIfNeeded(JobKey.LOGICAL_FLOW_CLEANUP_ORPHANS,
                (jk) -> logicalFlowService.cleanupOrphans());
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
