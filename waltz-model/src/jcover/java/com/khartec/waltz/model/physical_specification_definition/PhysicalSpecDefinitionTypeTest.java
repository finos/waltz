package com.khartec.waltz.model.physical_specification_definition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionType
 *
 * @author Diffblue JCover
 */

public class PhysicalSpecDefinitionTypeTest {

    @Test
    public void valuesReturnsDELIMITED() {
        assertThat(PhysicalSpecDefinitionType.values()[0], is(PhysicalSpecDefinitionType.DELIMITED));
    }
}
