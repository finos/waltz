package com.khartec.waltz.model.physical_specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for com.khartec.waltz.model.physical_specification.DataFormatKind
 *
 * @author Diffblue JCover
 */

public class DataFormatKindTest {

    @Test
    public void parseReturnsBINARY() {
        @SuppressWarnings("unchecked")
        Function<String, DataFormatKind> failedParseSupplier = mock(Function.class);
        when(failedParseSupplier.apply(Mockito.<String>any()))
            .thenReturn(DataFormatKind.BINARY);
        assertThat(DataFormatKind.parse("yyyy-MM-dd", failedParseSupplier), is(DataFormatKind.BINARY));
    }

    @Test
    public void valuesReturnsBINARYDATABASEFLAT_FILEJSONOTHERUNSTRUCTUREDUNKNOWNXML() {
        DataFormatKind[] result = DataFormatKind.values();
        assertThat(result[0], is(DataFormatKind.BINARY));
        assertThat(result[1], is(DataFormatKind.DATABASE));
        assertThat(result[2], is(DataFormatKind.FLAT_FILE));
        assertThat(result[3], is(DataFormatKind.JSON));
        assertThat(result[4], is(DataFormatKind.OTHER));
        assertThat(result[5], is(DataFormatKind.UNSTRUCTURED));
        assertThat(result[6], is(DataFormatKind.UNKNOWN));
        assertThat(result[7], is(DataFormatKind.XML));
    }
}
