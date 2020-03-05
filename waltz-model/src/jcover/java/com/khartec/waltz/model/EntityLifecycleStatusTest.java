package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.EntityLifecycleStatus
 *
 * @author Diffblue JCover
 */

public class EntityLifecycleStatusTest {

    @Test
    public void fromIsRemovedFlag() {
        assertThat(EntityLifecycleStatus.fromIsRemovedFlag(false), is(EntityLifecycleStatus.ACTIVE));
        assertThat(EntityLifecycleStatus.fromIsRemovedFlag(true), is(EntityLifecycleStatus.REMOVED));
    }

    @Test
    public void valuesReturnsACTIVEPENDINGREMOVED() {
        EntityLifecycleStatus[] result = EntityLifecycleStatus.values();
        assertThat(result[0], is(EntityLifecycleStatus.ACTIVE));
        assertThat(result[1], is(EntityLifecycleStatus.PENDING));
        assertThat(result[2], is(EntityLifecycleStatus.REMOVED));
    }
}
