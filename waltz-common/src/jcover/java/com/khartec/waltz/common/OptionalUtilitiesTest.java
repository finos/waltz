package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.OptionalUtilities
 *
 * @author Diffblue JCover
 */

public class OptionalUtilitiesTest {

    @Test
    public void contentsEqual() {
        assertThat(OptionalUtilities.<String>contentsEqual(Optional.of("foo"), "foo"), is(true));
        assertThat(OptionalUtilities.<String>contentsEqual(Optional.<String>empty(), "foo"), is(false));
        assertThat(OptionalUtilities.<String>contentsEqual(Optional.<String>empty(), null), is(true));
    }

    @Test
    public void maybeValueIsFooReturnsFoo() {
        assertThat(OptionalUtilities.<String>maybe("foo").get(), is("foo"));
    }

    @Test
    public void ofNullableOptional() {
        Optional<String> nullable = Optional.<String>empty();
        Optional<String> result = OptionalUtilities.<String>ofNullableOptional(nullable);
        assertThat(result.isPresent(), is(false));
        assertThat(nullable, sameInstance(result));
    }

    @Test
    public void ofNullableOptionalNullableIsNull() {
        assertThat(OptionalUtilities.<String>ofNullableOptional(null).isPresent(), is(false));
    }

    @Test
    public void toList() {
        assertTrue(OptionalUtilities.<String>toList(null).isEmpty());
        assertThat(OptionalUtilities.<String>toList(Optional.of("foo")).size(), is(1));
        assertThat(OptionalUtilities.<String>toList(Optional.of("foo")).get(0), is("foo"));
    }
}
