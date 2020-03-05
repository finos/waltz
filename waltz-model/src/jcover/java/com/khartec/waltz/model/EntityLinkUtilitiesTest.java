package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.EntityLinkUtilities
 *
 * @author Diffblue JCover
 */

public class EntityLinkUtilitiesTest {

    @Test
    public void mkExternalIdLinkExternalIdIsRootAndKindIsACTOR() {
        assertThat(EntityLinkUtilities.mkExternalIdLink("ZGF0YQ==", EntityKind.ACTOR, "root"), is("ZGF0YQ==entity/ACTOR/external-id/root"));
    }

    @Test
    public void mkIdLinkIdIsOneAndKindIsACTOR() {
        assertThat(EntityLinkUtilities.mkIdLink("ZGF0YQ==", EntityKind.ACTOR, 1L), is("ZGF0YQ==entity/ACTOR/id/1"));
    }
}
