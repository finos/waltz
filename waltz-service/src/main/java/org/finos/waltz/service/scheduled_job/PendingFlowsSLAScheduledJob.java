package org.finos.waltz.service.scheduled_job;

import org.finos.waltz.common.ExcludeFromIntegrationTesting;
import org.finos.waltz.data.scheduled_job.ScheduledJobDao;
import org.finos.waltz.model.scheduled_job.JobKey;
import org.finos.waltz.model.scheduled_job.JobLifecycleStatus;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

@ExcludeFromIntegrationTesting
@Service
public class PendingFlowsSLAScheduledJob {
    private static final Logger LOG = LoggerFactory.getLogger(PendingFlowsSLAScheduledJob.class);

    private final ScheduledJobDao scheduledJobDao;
    private final ProposedFlowWorkflowService proposedFlowWorkflowService;

    private static final String PENDING_FLOWS_TIME_OUT_THRESHOLD = "feature.data-flows-timeout-threshold";

    @Autowired
    public PendingFlowsSLAScheduledJob(ScheduledJobDao scheduledJobDao, ProposedFlowWorkflowService proposedFlowWorkflowService) {
        this.scheduledJobDao = scheduledJobDao;
        this.proposedFlowWorkflowService = proposedFlowWorkflowService;
    }

    @Scheduled(cron = "${waltz.proposed-flow.pending-timeout-cron:0 20 15 * * *}")
    public void run() {
        Thread.currentThread().setName("WaltzAutoTimeOutPendingJobsService");
        proposedFlowWorkflowService.timeOutPendingFlows(PENDING_FLOWS_TIME_OUT_THRESHOLD);
    }
    private void runIfNeeded(JobKey jobKey, Consumer<JobKey> jobExecutor) {
        runIfNeeded(jobKey, jobExecutor, Collections.emptySet());
    }

    private void runIfNeeded(JobKey jobKey, Consumer<JobKey> jobExecutor, Set<JobKey> deadlockJobKeys) {
        try {
            if (scheduledJobDao.isJobRunnable(jobKey)
                    && !scheduledJobDao.anyJobsRunning(deadlockJobKeys)
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
