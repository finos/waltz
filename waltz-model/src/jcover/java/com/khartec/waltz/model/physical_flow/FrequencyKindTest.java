package com.khartec.waltz.model.physical_flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for com.khartec.waltz.model.physical_flow.FrequencyKind
 *
 * @author Diffblue JCover
 */

public class FrequencyKindTest {

    @Test
    public void parseValueIsFooReturnsON_DEMAND() {
        @SuppressWarnings("unchecked")
        Function<String, FrequencyKind> failedParseSupplier = mock(Function.class);
        when(failedParseSupplier.apply(Mockito.<String>any()))
            .thenReturn(FrequencyKind.ON_DEMAND);
        assertThat(FrequencyKind.parse("foo", failedParseSupplier), is(FrequencyKind.ON_DEMAND));
    }
}
