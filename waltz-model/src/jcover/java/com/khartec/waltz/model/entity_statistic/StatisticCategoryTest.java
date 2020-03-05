package com.khartec.waltz.model.entity_statistic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.entity_statistic.StatisticCategory
 *
 * @author Diffblue JCover
 */

public class StatisticCategoryTest {

    @Test
    public void valuesReturnsCOMPLIANCEDATA_QUALITYGOVERNANCEREGULATORYSECURITYTECHNICAL() {
        StatisticCategory[] result = StatisticCategory.values();
        assertThat(result[0], is(StatisticCategory.COMPLIANCE));
        assertThat(result[1], is(StatisticCategory.DATA_QUALITY));
        assertThat(result[2], is(StatisticCategory.GOVERNANCE));
        assertThat(result[3], is(StatisticCategory.REGULATORY));
        assertThat(result[4], is(StatisticCategory.SECURITY));
        assertThat(result[5], is(StatisticCategory.TECHNICAL));
    }
}
