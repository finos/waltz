package com.khartec.waltz.model.scheduled_job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.scheduled_job.JobLifecycleStatus
 *
 * @author Diffblue JCover
 */

public class JobLifecycleStatusTest {

    @Test
    public void valuesReturnsRUNNABLERUNNINGCOMPLETEDERRORED() {
        JobLifecycleStatus[] result = JobLifecycleStatus.values();
        assertThat(result[0], is(JobLifecycleStatus.RUNNABLE));
        assertThat(result[1], is(JobLifecycleStatus.RUNNING));
        assertThat(result[2], is(JobLifecycleStatus.COMPLETED));
        assertThat(result[3], is(JobLifecycleStatus.ERRORED));
    }
}
