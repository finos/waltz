package com.khartec.waltz.model.system.job_log;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.system.job_log.JobStatus
 *
 * @author Diffblue JCover
 */

public class JobStatusTest {

    @Test
    public void valuesReturnsSUCCESSFAILUREIN_PROGRESS() {
        JobStatus[] result = JobStatus.values();
        assertThat(result[0], is(JobStatus.SUCCESS));
        assertThat(result[1], is(JobStatus.FAILURE));
        assertThat(result[2], is(JobStatus.IN_PROGRESS));
    }
}
