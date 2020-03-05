package com.khartec.waltz.model.enum_value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.enum_value.EnumValueKind
 *
 * @author Diffblue JCover
 */

public class EnumValueKindTest {

    @Test
    public void dbValueReturnsTransportKind() {
        assertThat(EnumValueKind.TRANSPORT_KIND.dbValue(), is("TransportKind"));
    }

    @Test
    public void valuesReturnsTRANSPORT_KINDBOOKMARK_KINDAUTHORITATIVENESS_RATINGCOST_KINDPERSON_KINDSCENARIO_TYPECHANGE_INITIATIVE_LIFECYCLE_PHASEPHYSICAL_FLOW_CRITICALITYFRESHNESS_INDICATOR() {
        EnumValueKind[] result = EnumValueKind.values();
        assertThat(result[0], is(EnumValueKind.TRANSPORT_KIND));
        assertThat(result[1], is(EnumValueKind.BOOKMARK_KIND));
        assertThat(result[2], is(EnumValueKind.AUTHORITATIVENESS_RATING));
        assertThat(result[3], is(EnumValueKind.COST_KIND));
        assertThat(result[4], is(EnumValueKind.PERSON_KIND));
        assertThat(result[5], is(EnumValueKind.SCENARIO_TYPE));
        assertThat(result[6], is(EnumValueKind.CHANGE_INITIATIVE_LIFECYCLE_PHASE));
        assertThat(result[7], is(EnumValueKind.PHYSICAL_FLOW_CRITICALITY));
        assertThat(result[8], is(EnumValueKind.FRESHNESS_INDICATOR));
    }
}
