package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.FieldDataType
 *
 * @author Diffblue JCover
 */

public class FieldDataTypeTest {

    @Test
    public void valuesReturnsDATEDATETIMEDECIMALINTEGERSTRINGBOOLEANENUMUNSPECIFIED() {
        FieldDataType[] result = FieldDataType.values();
        assertThat(result[0], is(FieldDataType.DATE));
        assertThat(result[1], is(FieldDataType.DATETIME));
        assertThat(result[2], is(FieldDataType.DECIMAL));
        assertThat(result[3], is(FieldDataType.INTEGER));
        assertThat(result[4], is(FieldDataType.STRING));
        assertThat(result[5], is(FieldDataType.BOOLEAN));
        assertThat(result[6], is(FieldDataType.ENUM));
        assertThat(result[7], is(FieldDataType.UNSPECIFIED));
    }
}
