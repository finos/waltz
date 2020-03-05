package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.RandomUtilities
 *
 * @author Diffblue JCover
 */

public class RandomUtilitiesTest {

    @Test
    public void randomlySizedIntStreamLowerIsZeroAndUpperIsOne() {
        int[] array = RandomUtilities.randomlySizedIntStream(0, 1).toArray();
        assertArrayEquals(new int[] { }, array);
    }

    @Test
    public void randomPick() {
        assertTrue(RandomUtilities.<String>randomPick(new LinkedList<String>(), 1).isEmpty());
        assertThat(RandomUtilities.<String>randomPick(new String[] { "foo" }), is("foo"));
    }

    @Test
    public void randomPickChoicesIsFooAndHowManyIsOneReturnsFoo() {
        Collection<String> choices = new LinkedList<String>();
        ((LinkedList<String>)choices).add("foo");
        assertThat(RandomUtilities.<String>randomPick(choices, 1).size(), is(1));
        assertThat(RandomUtilities.<String>randomPick(choices, 1).get(0), is("foo"));
    }

    @Test
    public void randomPickChoicesIsFooAndHowManyIsZeroReturnsEmpty() {
        Collection<String> choices = new LinkedList<String>();
        ((LinkedList<String>)choices).add("foo");
        assertTrue(RandomUtilities.<String>randomPick(choices, 0).isEmpty());
    }
}
