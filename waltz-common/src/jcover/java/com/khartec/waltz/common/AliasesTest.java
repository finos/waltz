package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.Aliases
 *
 * @author Diffblue JCover
 */

public class AliasesTest {

    @Test
    public void lookupAliasIsFoo() {
        assertThat(new Aliases<String>().lookup("foo").isPresent(), is(false));
    }

    @Test
    public void registerAliasesIsFooAndValIsFoo() {
        Aliases<String> aliases = new Aliases<String>();
        assertThat(aliases.register("foo", "foo"), sameInstance(aliases));
    }
}
