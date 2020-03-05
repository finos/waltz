package com.khartec.waltz.model.scheduled_job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.scheduled_job.JobKey
 *
 * @author Diffblue JCover
 */

public class JobKeyTest {

    @Test
    public void valuesReturnsHIERARCHY_REBUILD_CHANGE_INITIATIVEHIERARCHY_REBUILD_DATA_TYPEHIERARCHY_REBUILD_ENTITY_STATISTICSHIERARCHY_REBUILD_MEASURABLEHIERARCHY_REBUILD_ORG_UNITHIERARCHY_REBUILD_PERSONDATA_TYPE_RIPPLE_PHYSICAL_TO_LOGICALDATA_TYPE_USAGE_RECALC_APPLICATIONCOMPLEXITY_REBUILDAUTH_SOURCE_RECALC_FLOW_RATINGSLOGICAL_FLOW_CLEANUP_ORPHANSATTESTATION_CLEANUP_ORPHANS() {
        JobKey[] result = JobKey.values();
        assertThat(result[0], is(JobKey.HIERARCHY_REBUILD_CHANGE_INITIATIVE));
        assertThat(result[1], is(JobKey.HIERARCHY_REBUILD_DATA_TYPE));
        assertThat(result[2], is(JobKey.HIERARCHY_REBUILD_ENTITY_STATISTICS));
        assertThat(result[3], is(JobKey.HIERARCHY_REBUILD_MEASURABLE));
        assertThat(result[4], is(JobKey.HIERARCHY_REBUILD_ORG_UNIT));
        assertThat(result[5], is(JobKey.HIERARCHY_REBUILD_PERSON));
        assertThat(result[6], is(JobKey.DATA_TYPE_RIPPLE_PHYSICAL_TO_LOGICAL));
        assertThat(result[7], is(JobKey.DATA_TYPE_USAGE_RECALC_APPLICATION));
        assertThat(result[8], is(JobKey.COMPLEXITY_REBUILD));
        assertThat(result[9], is(JobKey.AUTH_SOURCE_RECALC_FLOW_RATINGS));
        assertThat(result[10], is(JobKey.LOGICAL_FLOW_CLEANUP_ORPHANS));
        assertThat(result[11], is(JobKey.ATTESTATION_CLEANUP_ORPHANS));
    }
}
