package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.StringUtilities
 *
 * @author Diffblue JCover
 */

public class StringUtilitiesTest {

    @Test
    public void firstChar() {
        assertThat(StringUtilities.firstChar("", '\u0000'), is('\u0000'));
        assertThat(StringUtilities.firstChar("Anna", 'a'), is('A'));
        assertThat(StringUtilities.firstChar("", 'a'), is('a'));
        assertThat(StringUtilities.firstChar(null, 'a'), is('a'));
    }

    @Test
    public void ifEmpty() {
        assertThat(StringUtilities.ifEmpty("bar", "foo"), is("bar"));
        assertThat(StringUtilities.ifEmpty("", "foo"), is("foo"));
    }

    @Test
    public void isEmpty() {
        assertThat(StringUtilities.isEmpty(Optional.<String>empty()), is(true));
        assertThat(StringUtilities.isEmpty(Optional.of("foo")), is(false));
        assertThat(StringUtilities.isEmpty(""), is(true));
        assertThat(StringUtilities.isEmpty("foo"), is(false));
        assertThat(StringUtilities.isEmpty((String)null), is(true));
    }

    @Test
    public void isNumericLong() {
        assertThat(StringUtilities.isNumericLong(""), is(false));
        assertThat(StringUtilities.isNumericLong("foo"), is(false));
    }

    @Test
    public void joinValuesIsEmptyReturnsEmpty() {
        assertThat(StringUtilities.join(new LinkedList<Aliases>(), ","), is(""));
    }

    @Test
    public void length() {
        assertThat(StringUtilities.length(""), is(0));
        assertThat(StringUtilities.length("foo"), is(3));
        assertThat(StringUtilities.length(null), is(0));
    }

    @Test
    public void limit() {
        assertThat(StringUtilities.limit("foo", 1), is("f"));
        assertThat(StringUtilities.limit(null, 1), is(nullValue()));
    }

    @Test
    public void lowerValueIsFooReturnsFoo() {
        assertThat(StringUtilities.lower("foo"), is("foo"));
    }

    @Test
    public void mkSafeStrIsFooReturnsFoo() {
        assertThat(StringUtilities.mkSafe("foo"), is("foo"));
    }

    @Test
    public void notEmpty() {
        assertThat(StringUtilities.notEmpty(""), is(false));
        assertThat(StringUtilities.notEmpty("foo"), is(true));
    }

    @Test
    public void parseIntegerDfltIsOneAndValueIsFooReturnsOne() {
        assertThat(StringUtilities.parseInteger("foo", 1), is(1));
    }

    @Test
    public void tokenise() {
        assertTrue(StringUtilities.tokenise("").isEmpty());
        assertThat(StringUtilities.tokenise("foo").size(), is(1));
        assertThat(StringUtilities.tokenise("foo").get(0), is("foo"));
    }

    @Test
    public void toOptionalStrIsEmpty() {
        assertThat(StringUtilities.toOptional("").isPresent(), is(false));
    }

    @Test
    public void toOptionalStrIsFooReturnsFoo() {
        assertThat(StringUtilities.toOptional("foo").get(), is("foo"));
    }
}
